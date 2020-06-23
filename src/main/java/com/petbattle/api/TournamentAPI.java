package com.petbattle.api;

import com.petbattle.core.PetVote;
import io.quarkus.launcher.shaded.org.slf4j.Logger;
import io.quarkus.launcher.shaded.org.slf4j.LoggerFactory;
import io.smallrye.mutiny.Uni;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/tournament")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TournamentAPI {

    private final Logger log = LoggerFactory.getLogger(TournamentAPI.class);


    @Inject
    EventBus bus;

    @POST
    public Uni<Integer> createTournament() {
        return bus.<Integer>request("CreateTournament", "create")
                .onItem().apply(Message::body);
    }

    @GET
    @Path("{id}/leaderboard ")
    public List<PetVote> leaderboard(@PathParam("id") int tournamentID) {
//        ArrayList<PetVote> res = new ArrayList<>();
//        Uni<JsonObject> x = bus.<JsonObject>request("CreateTournament", "create")
//                .onItem().apply(Message::body);
//        x.subscribe().with(
//                result -> System.out.println("result is " + result.encodePrettily()),
//                failure -> failure.printStackTrace()
//        );


        bus.<JsonObject>request("CreateTournament", "create")
                .onItem().apply(body -> {
            ArrayList<PetVote> res = new ArrayList<>();
            res = body.body().mapTo(ArrayList.class);
            return res;
        });
        return null;
    }

    @POST
    @Path("{id}/start")
    public void startTournament(@PathParam("id") int tournamentID) {
        return;
    }

    @POST
    @Path("{id}/stop")
    public void stopTournament(@PathParam("id") int tournamentID) {
        return;
    }
}
