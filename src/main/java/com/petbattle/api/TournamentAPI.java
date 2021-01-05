package com.petbattle.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.petbattle.core.PetVote;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.swagger.v3.oas.annotations.Operation;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


@Path("/api/tournament")
@SecurityScheme(securitySchemeName = "jwt", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "jwt")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentAPI {
    private final Logger log = LoggerFactory.getLogger(TournamentAPI.class);
    private ObjectMapper jsonMapper;
    private CollectionType javaType;
    private final MeterRegistry registry;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    Template leaderboard;

    @Inject
    EventBus bus;

    public TournamentAPI(MeterRegistry registry) {
        jsonMapper = new ObjectMapper();
        javaType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, PetVote.class);
        this.registry = registry;
    }

    @POST
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Create a new tournament or return the existing tournament")
    public Uni<JsonObject> createTournament() {
        log.info("Creating tournament");
        return bus.<JsonObject>request("CreateTournament", "")
                .onItem().invoke(() -> registry.counter("TournamentCreated", Tags.empty()).increment())
                .onItem().transform(Message::body);
    }

    @GET
    //@RolesAllowed("pbplayer")
    //@SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Return the existing tournament")
    public Uni<JsonObject> getTournament(String name) {
        log.info("Get tournament");
        return bus.<JsonObject>request("GetTournament", "")
                .onItem().invoke(() -> registry.counter("GetTournament", Tags.empty()).increment())
                .onItem().transform(Message::body);
    }

    @GET
    @Path("{id}")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Return Tournament Status")
    public Uni<JsonObject> tournamentStatus(@PathParam("id") String tournamentID) {
        log.info("Get status for tournament {}", tournamentID);

        Uni<JsonObject> res = bus.<JsonObject>request("GetTournamentStatus", tournamentID)
                .onItem().invoke(() -> registry.counter("TournamentStatus", Tags.empty()).increment())
                .onItem().transform(Message::body);

        return res;
    }

    @GET
    @Path("{id}/leaderboard")
    //@RolesAllowed("pbplayer")
    //@SecurityRequirement(name="jwt", scopes = {})
    @Timed
    public Uni<List<PetVote>> leaderboard(@PathParam("id") String tournamentID) {
        log.info("Get leaderboard for tournament {}", tournamentID);

        Uni<String> res = bus.<String>request("GetLeaderboard", tournamentID)
                .onItem().transform(Message::body);
        registry.counter("TournamentLeaderboard", Tags.empty()).increment();
        return res.onItem().transform(result -> {
            List<PetVote> lb = new ArrayList<>();
            try {
                lb = jsonMapper.readValue(result, javaType);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return lb;
        });
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Start a tournament")
    public Uni<Object> startTournament(@PathParam("id") String tournamentID) {
        log.info("Start tournament {}", tournamentID);
        return bus.<JsonObject>request("StartTournament", tournamentID)
                .onItem().invoke(() -> registry.counter("TournamentStart", Tags.empty()).increment())
                .onItem().transform(Message::body);
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Stop a tournament")
    public Uni<Object> stopTournament(@PathParam("id") String tournamentID) {
        log.info("Stop tournament {}", tournamentID);
        return bus.<JsonObject>request("StopTournament", tournamentID)
                .onItem().invoke(() -> registry.counter("TournamentStopped", Tags.empty()).increment())
                .onItem().transform(Message::body);
    }

    @DELETE
    @Path("{id}/cancel")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name="jwt", scopes = {})
    @Operation(summary = "Cancel a tournament")
    public void cancelTournament(@PathParam("id") String tournamentID) {
        log.info("Cancel tournament {}", tournamentID);
        registry.counter("TournamentCancelled", Tags.empty()).increment();
        bus.sendAndForget("CancelCurrentTournament", tournamentID);
    }

    @POST
    @Path("{id}/add/{petId}")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Add a pet to a tournament")
    public Uni<Object> addPetToTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID) {
        log.info("addPetToTournament {}:{}", tournamentID, petID);
        JsonObject params = new JsonObject();
        params.put("tournamentId", tournamentID);
        params.put("petId", petID);
        return bus.<JsonObject>request("AddPetToTournament", params)
                .onItem().invoke(() -> registry.counter("TournamentPetsAdded", Tags.of("TID",tournamentID)).increment())
                .onItem().transform(Message::body);
    }

    @POST
    @Path("{id}/vote/{petId}")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Vote for a pet in a tournament")
    public Uni<Response> voteForPetInTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID, @NotNull @QueryParam("dir") String dir) {
        log.info("VotePetInTournament {}:{} Dir{}", tournamentID, petID, dir);
        if ((!dir.equalsIgnoreCase("up")) && (!dir.equalsIgnoreCase("down")))
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)).onItem().transform(Response.ResponseBuilder::build);
        JsonObject params = new JsonObject();
        params.put("timestamp", System.currentTimeMillis());
        params.put("tournamentId", tournamentID);
        params.put("petId", petID);
        params.put("dir", dir);

        return bus.<JsonObject>request("ProcessPetVote", params)
                .onItem().invoke(() -> registry.counter("TournamentPetVote",
                        Tags.of("TID",tournamentID).and("DIR",dir.toUpperCase())).increment())
                .onItem().transform(b -> Response.ok(b.body()).build())
                .onFailure().recoverWithUni(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));
    }

    @GET
    @Path("{id}/votes/{petId}")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Return the number of votes for a pet in a tournament")
    public Uni<Response> getVotesForPetInTournament( @PathParam("id") String tournamentID,@PathParam("petId") String petID) {
        log.info("getVotesForPetInTournament {}", petID);
        JsonObject params = new JsonObject();
        params.put("petId", petID);
        params.put("tournamentId", tournamentID);
        return bus.<JsonObject>request("GetPetVote", params)
                .onItem().invoke(() -> registry.counter("TournamentPetVote",
                        Tags.of("TID",tournamentID).and("ACTION","GET")).increment())
                .onItem().transform(b -> Response.ok(b.body()).build())
                .onFailure().recoverWithUni(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    @Path("leaderboard")
    //@RolesAllowed("pbplayer")
    //@SecurityRequirement(name="jwt", scopes = {})
    @Timed
    @Operation(summary = "Return the leaderboard for a tournament")
    public TemplateInstance leaderboardUX() {
        String tid = getTournament("").await().indefinitely().getString("TournamentID");
        if (null == tid) {
            registry.counter("GetLeaderboard", Tags.empty()).increment();
            return leaderboard.data("pets", new ArrayList());
        }
        registry.counter("GetLeaderboard", Tags.of("TID", tid)).increment();
        return leaderboard.data("pets", leaderboard(tid).await().indefinitely());
    }

}
