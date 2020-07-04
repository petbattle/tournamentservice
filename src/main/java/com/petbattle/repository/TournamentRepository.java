package com.petbattle.repository;

import com.petbattle.core.Tournament;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TournamentRepository implements PanacheMongoRepository<Tournament> {
}
