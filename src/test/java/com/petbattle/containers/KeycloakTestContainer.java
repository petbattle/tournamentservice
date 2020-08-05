package com.petbattle.containers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class KeycloakTestContainer implements QuarkusTestResourceLifecycleManager {
    private static final KeycloakContainer SSO = new KeycloakContainer("jboss/keycloak:latest").withRealmImportFile("pbrealm-test.json");

    @Override
    public Map<String, String> start() {
        final Map<String, String> res = new HashMap<>();
        SSO.start();
        Keycloak keycloakAdminClient = KeycloakBuilder.builder()
                .serverUrl(SSO.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(SSO.getAdminUsername())
                .password(SSO.getAdminPassword())
                .build();


        addPlayer(keycloakAdminClient);
        addAdmin(keycloakAdminClient);

        RealmResource realmResource = keycloakAdminClient.realm("pbrealm");
        ClientRepresentation app1Client = realmResource.clients().findByClientId("pbclient").get(0);

        String sec = realmResource.clients().get(app1Client.getId()).getSecret().getValue();

        res.put("quarkus.oidc.auth-server-url", SSO.getAuthServerUrl() + "/realms/pbrealm");
        res.put("quarkus.oidc.credentials.secret",sec);
        res.put("quarkus.pbclient.test.secret",sec);
        return res;
    }

    private void addPlayer(Keycloak keycloakAdminClient)
    {
        // Define pbPlayer
        UserRepresentation pbPlayer = new UserRepresentation();
        pbPlayer.setEnabled(true);
        pbPlayer.setUsername("player1");
        pbPlayer.setFirstName("First");
        pbPlayer.setLastName("Last");
        pbPlayer.setEmail("player1@petbattle.com");

        // Get realm
        RealmResource realmResource = keycloakAdminClient.realm("pbrealm");
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(pbPlayer);
        String userId = CreatedResponseUtil.getCreatedId(response);
        UserResource userResource = usersResource.get(userId);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue("player1pwd");
        userResource.resetPassword(passwordCred);

        ClientRepresentation app1Client = realmResource.clients().findByClientId("pbclient").get(0);

        RoleRepresentation userClientRole = realmResource.clients().get(app1Client.getId())
                .roles().get("pbplayer").toRepresentation();
        userResource.roles().clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole));
    }

    private void addAdmin(Keycloak keycloakAdminClient)
    {
        // Define pbPlayer
        UserRepresentation pbAdmin = new UserRepresentation();
        pbAdmin.setEnabled(true);
        pbAdmin.setUsername("pbadmin");
        pbAdmin.setFirstName("pbadmin");
        pbAdmin.setLastName("pbadmin");
        pbAdmin.setEmail("pbAdmin@petbattle.com");
        // Get realm
        RealmResource realmResource = keycloakAdminClient.realm("pbrealm");
        UsersResource usersResource = realmResource.users();
        Response response = usersResource.create(pbAdmin);
        String userId = CreatedResponseUtil.getCreatedId(response);
        UserResource userResource = usersResource.get(userId);

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue("pbadminpwd");
        userResource.resetPassword(passwordCred);

        ClientRepresentation app1Client = realmResource.clients().findByClientId("pbclient").get(0);

        RoleRepresentation userClientRole = realmResource.clients().get(app1Client.getId())
                .roles().get("pbadmin").toRepresentation();
        userResource.roles().clientLevel(app1Client.getId()).add(Arrays.asList(userClientRole));
    }

    @Override
    public void stop() {
        SSO.stop();
    }
}