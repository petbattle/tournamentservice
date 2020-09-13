package com.petbattle.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.petbattle.core.PetVote;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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

@OpenAPIDefinition(
        tags = {
                @Tag(name = "tournament", description = "tournament operations."),
        },
        info = @Info(
                title = "Tournament API",
                version = "1.0.1",
                contact = @Contact(
                        name = "Tournament API Support",
                        url = "http://petbattle.com/contact",
                        email = "techsupport@petbattle.com"),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"))
)
@Path("/tournament")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentAPI {
    private final Logger log = LoggerFactory.getLogger(TournamentAPI.class);
    private ObjectMapper jsonMapper;
    private CollectionType javaType;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    Template leaderboard;

    @Inject
    EventBus bus;

    public TournamentAPI() {
        jsonMapper = new ObjectMapper();
        javaType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, PetVote.class);
    }

    @POST
//    @RolesAllowed("pbadmin")
    @Counted(name = "tournamentsCreated", description = "How many tournaments have been created.")
    public Uni<JsonObject> createTournament() {
        log.info("Creating tournament");
        return bus.<JsonObject>request("CreateTournament", "")
                .onItem().apply(Message::body);
    }

    @GET
    @Path("{id}")
//    @RolesAllowed("pbplayer")
    public Uni<JsonObject> tournamentStatus(@PathParam("id") String tournamentID) {
        log.info("Get status for tournament {}", tournamentID);

        Uni<JsonObject> res = bus.<JsonObject>request("GetTournamentStatus", tournamentID)
                .onItem().apply(Message::body);

        return res;
    }

    @GET
    @Path("{id}/leaderboard")
//    @RolesAllowed("pbplayer")
    @Timed(name = "getLeaderboardTimer", description = "A measure of how long it takes to get the leaderboard values", unit = MetricUnits.MILLISECONDS)
    public Uni<List<PetVote>> leaderboard(@PathParam("id") String tournamentID) {
        log.info("Get leaderboard for tournament {}", tournamentID);

        Uni<String> res = bus.<String>request("GetLeaderboard", tournamentID)
                .onItem().apply(Message::body);

        return res.onItem().apply(result -> {
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
//    @RolesAllowed("pbadmin")
    @Counted(name = "tournamentsStarted", description = "How many tournaments have been started.")
    public Uni<Object> startTournament(@PathParam("id") String tournamentID) {
        log.info("Start tournament {}", tournamentID);
        return bus.<JsonObject>request("StartTournament", tournamentID)
                .onItem().apply(Message::body);
    }

    @DELETE
    @Path("{id}")
//    @RolesAllowed("pbadmin")
    @Counted(name = "tournamentsStopped", description = "How many tournaments have been stopped.")
    public Uni<Object> stopTournament(@PathParam("id") String tournamentID) {
        log.info("Stop tournament {}", tournamentID);
        return bus.<JsonObject>request("StopTournament", tournamentID)
                .onItem().apply(Message::body);
    }

    @DELETE
    @Path("{id}/cancel")
//    @RolesAllowed("pbadmin")
    @Counted(name = "tournamentsCancelled", description = "How many tournaments have been cancelled.")
    public void cancelTournament(@PathParam("id") String tournamentID) {
        log.info("Cancel tournament {}", tournamentID);
        bus.sendAndForget("CancelCurrentTournament", tournamentID);
    }

    @POST
    @Path("{id}/add/{petId}")
//    @RolesAllowed("pbadmin")
    @Counted(name = "petsAdded", description = "How many pets have been added to tournaments")
    public Uni<Object> addPetToTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID) {
        log.info("addPetToTournament {}:{}", tournamentID, petID);
        JsonObject params = new JsonObject();
        params.put("tournamentId", tournamentID);
        params.put("petId", petID);
        return bus.<JsonObject>request("AddPetToTournament", params)
                .onItem().apply(Message::body);
    }

    @POST
    @Path("{id}/vote/{petId}")
//    @RolesAllowed("pbplayer")
    @Counted(name = "votes", description = "How many votes have been cast.")
    @Timed(name = "castVoteTimer", description = "A measure of how long it takes to cast a vote for for a pet.", unit = MetricUnits.MILLISECONDS)
    public Uni<Response> voteForPetInTournament(@PathParam("id") String tournamentID, @PathParam("petId") String petID, @NotNull @QueryParam("dir") String dir) {
        log.info("VotePetInTournament {}:{} Dir{}", tournamentID, petID, dir);
        if ((!dir.equalsIgnoreCase("up")) && (!dir.equalsIgnoreCase("down")))
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)).onItem().apply(Response.ResponseBuilder::build);
        JsonObject params = new JsonObject();
        params.put("timestamp", System.currentTimeMillis());
        params.put("tournamentId", tournamentID);
        params.put("petId", petID);
        params.put("dir", dir);
        return bus.<JsonObject>request("ProcessPetVote", params)
                .onItem().apply(b -> Response.ok(b.body()).build())
                .onFailure().recoverWithUni(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));
    }

    @GET
    @Path("{id}/votes/{petId}")
//    @RolesAllowed("pbplayer")
    @Timed(name = "getVotesTimer", description = "A measure of how long it takes to get votes for a pet.", unit = MetricUnits.MILLISECONDS)
    public Uni<Response> getVotesForPetInTournament( @PathParam("id") String tournamentID,@PathParam("petId") String petID) {
        log.info("getVotesForPetInTournament {}", petID);
        JsonObject params = new JsonObject();
        params.put("petId", petID);
        params.put("tournamentId", tournamentID);
        return bus.<JsonObject>request("GetPetVote", params)
                .onItem().apply(b -> Response.ok(b.body()).build())
                .onFailure().recoverWithUni(Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build()));
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    @Path("leaderboard/{id}")
//    @RolesAllowed("pbplayer")
    @Timed(name = "getLBTimer", description = "A measure of how long it takes to get the leaderboard values for a tournament", unit = MetricUnits.MILLISECONDS)
    public TemplateInstance leaderboardUX(@PathParam("id") String tournamentID) {
        return leaderboard.data("pets", leaderboard(tournamentID).await().indefinitely());
    }

}
