package com.petbattle.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.petbattle.core.PetVote;
import com.petbattle.exceptions.TournamentException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
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
    private final MeterRegistry registry;
    @Inject
    SecurityIdentity securityIdentity;
    @Inject
    Template leaderboardux;
    @Inject
    EventBus bus;
    private ObjectMapper jsonMapper;
    private CollectionType javaType;

    public TournamentAPI(MeterRegistry registry) {
        jsonMapper = new ObjectMapper();
        javaType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, PetVote.class);
        this.registry = registry;
    }

    @POST
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Create a new tournament or return the existing tournament")
    @APIResponses(value = { @APIResponse(responseCode = "200", description = "Tournament created/retrieved") })
    public Uni<Response> createTournament() {
        log.info("Creating tournament");
        registry.counter("TournamentCreated", Tags.empty()).increment();
        return bus.<JsonObject> request("CreateTournament", "")
                .map(x -> Response.ok(x.body()).build());
    }

    @GET
    @Timed
    @Operation(summary = "Return the existing tournament")
    @APIResponses(value = { @APIResponse(responseCode = "200", description = "Tournament id retrieved") })
    public Uni<Response> getTournament(String name) {
        log.info("Get tournament");
        registry.counter("GetTournament", Tags.empty()).increment();
        return bus.<JsonObject> request("GetTournament", "")
                .map(x -> Response.ok(x.body()).build());
    }

    @GET
    @Path("{id}")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Return Tournament Status")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Tournament status"),
            @APIResponse(responseCode = "404", description = "Tournament not found") })
    public Uni<Response> tournamentStatus(@PathParam("id") String tournamentID) {
        log.info("Get status for tournament {}", tournamentID);
        registry.counter("TournamentStatus", Tags.empty()).increment();
        return bus.<JsonObject> request("GetTournamentStatus", tournamentID)
                .map(x -> Response.ok(x.body()).build())
                .onFailure(TournamentException.class)
                .recoverWithItem(failure -> (Response.serverError().status(Response.Status.NOT_FOUND.getStatusCode(),
                        failure.getMessage())).build());
    }

    @PUT
    @Path("{id}")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Start a tournament")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Tournament started"),
            @APIResponse(responseCode = "400", description = "Invalid tournament id passed or tournament already started") })
    public Uni<Response> startTournament(@PathParam("id") String tournamentID) {
        log.info("Start tournament {}", tournamentID);
        registry.counter("TournamentStart", Tags.empty()).increment();
        return bus.<Void> request("StartTournament", tournamentID)
                .onItem().transform(b -> Response.ok(b.body()).build())
                .onFailure(TournamentException.class)
                .recoverWithItem(failure -> (Response.serverError().status(Response.Status.BAD_REQUEST.getStatusCode(),
                        failure.getMessage())).build());
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Stop a tournament")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Tournament started"),
            @APIResponse(responseCode = "400", description = "Invalid tournament id passed or tournament not started") })
    public Uni<Response> stopTournament(@PathParam("id") String tournamentID) {
        log.info("Stop tournament {}", tournamentID);
        registry.counter("TournamentStopped", Tags.empty()).increment();
        return bus.<Void> request("StopTournament", tournamentID)
                .onItem().transform(b -> Response.ok().build())
                .onFailure(TournamentException.class)
                .recoverWithItem(failure -> (Response.serverError().status(Response.Status.BAD_REQUEST.getStatusCode(),
                        failure.getMessage())).build());
    }

    @DELETE
    @Path("{id}/cancel")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Operation(summary = "Cancel a tournament")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Tournament cancelled") })
    public void cancelTournament(@PathParam("id") String tournamentID) {
        log.info("Cancel tournament {}", tournamentID);
        registry.counter("TournamentCancelled", Tags.empty()).increment();
        bus.publish("CancelCurrentTournament", tournamentID);
    }

    @POST
    @Path("{id}/add/{petId}")
    @RolesAllowed("pbadmin")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Add a pet to a tournament")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Pet added to tournament"),
            @APIResponse(responseCode = "400", description = "Exception during processing") })
    public Uni<Response> addPetToTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID) {
        log.info("addPetToTournament {}:{}", tournamentID, petID);
        registry.counter("TournamentPetsAdded", Tags.of("TID", tournamentID)).increment();
        JsonObject params = new JsonObject();
        params.put("tournamentId", tournamentID);
        params.put("petId", petID);

        return bus.<Void> request("AddPetToTournament", params)
                .map(x -> Response.ok().build())
                .onFailure(TournamentException.class)
                .recoverWithItem(failure -> (Response.serverError().status(Response.Status.BAD_REQUEST.getStatusCode(),
                        failure.getMessage())).build());
    }

    @POST
    @Path("{id}/vote/{petId}")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Vote for a pet in a tournament")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Vote for pet registered"),
            @APIResponse(responseCode = "400", description = "Exception during processing") })
    public Uni<Response> voteForPetInTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID,
            @NotNull @QueryParam("dir") String dir) {
        log.info("VotePetInTournament {}:{} Dir{}", tournamentID, petID, dir);
        if ((!dir.equalsIgnoreCase("up")) && (!dir.equalsIgnoreCase("down")))
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)).onItem()
                    .transform(Response.ResponseBuilder::build);
        JsonObject params = new JsonObject();
        params.put("timestamp", System.currentTimeMillis());
        params.put("tournamentId", tournamentID);
        params.put("petId", petID);
        params.put("dir", dir);
        registry.counter("PetVotes", Tags.of("TID", tournamentID).and("DIR", dir.toUpperCase())).increment();

        return bus.<Void> request("ProcessPetVote", params)
                .onItem().transform(b -> Response.ok().build())
                .onFailure().recoverWithUni(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));
    }

    @GET
    @Path("{id}/votes/{petId}")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Return the number of votes for a pet in a tournament")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Vote for pet"),
            @APIResponse(responseCode = "400", description = "Exception during processing") })
    public Uni<Response> getVotesForPetInTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID) {
        log.info("getVotesForPetInTournament {}", petID);
        registry.counter("GetPetVote", Tags.of("TID", tournamentID).and("ACTION", "GET")).increment();
        JsonObject params = new JsonObject();
        params.put("petId", petID);
        params.put("tournamentId", tournamentID);
        return bus.<JsonObject> request("GetPetVote", params)
                .onItem().transform(b -> Response.ok(b.body()).build())
                .onFailure().recoverWithUni(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    @Path("leaderboardux")
    @RolesAllowed("pbplayer")
    @SecurityRequirement(name = "jwt", scopes = {})
    @Timed
    @Operation(summary = "Return the leaderboard for a tournament")
    public TemplateInstance leaderboardUX() {
        registry.counter("GetLeaderboardUX", Tags.empty()).increment();
        String tid = bus.<JsonObject> request("GetTournament", "").await().indefinitely().body().getString("TournamentID");
        if (null == tid) {
            return leaderboardux.data("pets", new ArrayList());
        }
        Uni<List<PetVote>> petdata = bus.<String> request("GetLeaderboard", tid)
                .onItem().transform(result -> {
                    List<PetVote> lb = new ArrayList<>();
                    try {
                        lb = jsonMapper.readValue(result.body(), javaType);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return lb;
                });

        return leaderboardux.data("pets", petdata.await().indefinitely());
    }

    @GET
    @Path("/leaderboard")
    //    @RolesAllowed("pbplayer")
    //    @SecurityRequirement(name = "jwt", scopes = {})
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Tournament leaderboard"),
            @APIResponse(responseCode = "501", description = "Internal server error") })
    public Uni<Response> leaderboard(@PathParam("id") String tournamentID) {
        log.info("Get leaderboard for tournament {}", tournamentID);
        registry.counter("GetLeaderboard", Tags.empty()).increment();

        return bus.<String> request("GetLeaderboard", tournamentID)
                .onItem().transform(result -> {
                    List<PetVote> lb = new ArrayList<>();
                    try {
                        lb = jsonMapper.readValue(result.body(), javaType);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return lb;
                }).map(x -> Response.ok(x).build())
                .onFailure()
                .recoverWithItem(failure -> (Response.serverError()
                        .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), failure.getMessage())).build());
    }
}
