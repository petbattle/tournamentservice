package com.petbattle.core;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(includeClasses = { PetVote.class }, schemaPackageName = "pet_vote")
interface PetVoteProtoInitialiser extends SerializationContextInitializer {
}
