# Remerciements

image:https://img.shields.io/badge/vert.x-4.3.4-purple.svg[link="https://vertx.io"]

Cette application a été générée grâce à http://start.vertx.io

# Utilisation

## Fonctionnement général

Tout appel aux routes préfixées par `/protected/` requiert une authentification basique HTTP et déclencheront la détection
du changement d'appareil.
Tout appel aux routes préfixées par `/public/` est accessible sans authentification et ne déclenche pas la détection du
changement d'appareil.

### Règles de gestion

* Si l'utilisateur n'a pas d'authentification HTTP basique alors une erreur 401 est retournée.
* Si c'est la première connexion de l'utilisateur alors ses paramètres de connexion sont enregistrés ssi l'en-tête
user-agent est présent.
* Si des paramètres de connexion ont déjà été enregistrés alors on comparera les paramètres de la connexion courante à
ceux enregistrés la première fois pour l'utilisateur en suivant les règles définies dans l'énoncé du problème.

### Exemples d'appels

#### Appel non authentifié (mauvais mot de passe)

```
curl -X GET http://localhost:8000/protected/foo  -H "Authorization: Basic amxlb2Jlcm5hcmRAZ21haWwuY29tOiBwYXNzd29yZDE=" -v
```

#### Appel authentifié

```
curl -X GET http://localhost:8000/protected/foo  -H "Authorization: Basic amxlb2Jlcm5hcmRAZ21haWwuY29tOnBhc3N3b3JkMb==" -v
Note: Unnecessary use of -X or --request, GET is already inferred.
*   Trying 127.0.0.1:8000...
* Connected to localhost (127.0.0.1) port 8000 (#0)
> GET /protected/foo HTTP/1.1
> Host: localhost:8000
> User-Agent: curl/7.79.1
> Accept: */*
> Authorization: Basic amxlb2Jlcm5hcmRAZ21haWwuY29tOnBhc3N3b3JkMb==
>
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< content-type: application/json
< content-length: 91
<
* Connection #0 to host localhost left intact
{"success":true,"message":"Access granted to jleobernard@gmail.com to path /protected/foo"}
```


# Architecture

Le détecteur de changement d'appareil de connexion de l'utilisateur se base sur l'ajout d'un `RequestHandler`. Dès lors
que cet *handler* est ajouté à une route, les opérations suivantes vont être déclenchées :

1. Récupération de l'IP et du user-agent de la requête et envoi de ces informations sont ensuite envoyées sur le bus.
2. Ces informations brutes sont récupérées par le *parser* de user-agent.
(qui s'appuye sur la librairie com.github.ua-parser.uap-java) qui va concaténer les informations extraites et les envoyer
sur le bus.
3. Les informations parsées sont récupérées par le service de comparaison qui va d'abord récupérer les informations déjà
enregistrées pour l'utilisateur puis parcourir tous les critères de comparaison afin de détecter les divergences. Pour
chaque divergence trouvée, le nombre de points associé au critère de comparaison spécifié dans `conf/config.json`
(attribut `weights`) est ajouté.<br />
Une fois le score et les différences obtenus, le service de comparaison va publier une alerte sur le bus ssi le score
calculé est supérieur au seuil spécifié dans le fichier `conf/config.json` (attribut `threshold`).
4. Le service d'alerte écoute le bus pour récupérer les alertes publiées, les sérialise en json, et les ajoute au
fichier dont le chemin est spécifié dans le fichier `conf/config.json` (attribut `alertFile`). Si ce fichier n'existe
pas il est créé.

# Limitations

* Les utilisateurs s'authentifient via le mécanisme d'authentification basique HTTP, il est donc crucial que la requête
ne passe pas sur un réseau public en HTTP (utiliser par exemple un reverse proxy https).
* Les mots de passe des utilisateurs sont en clair dans le fichier `conf/config.json` (attributs `userCredentials`) afin de faciliter la création du
mock du service d'authentification. En production il faudrait donc que le verticle en charge de l'authentification
s'appuye sur un système d'authentification externe ou une base de données dans laquelle le mot de passe des utilisateurs
aura été préalablement chiffré et salé.
* Les changements de configuration ne sont pas pris en compte à chaud.
* L'en-tête user-agent est un champ non normé et il est donc difficile d'extraire tous les champs désirés.
Grâce aux librairies [uap-java](https://github.com/ua-parser/uap-java)  et [UserAgentUtils](https://github.com/HaraldWalker/user-agent-utils) les
champs suivants ont été traités :
    * device
    * device_brand
    * OS_name
    * OS_family
    * OS_version
    * Client_type
    * Client_name
    * Client_version
    * IP
* Les informations de connexion des utilisateurs sont stockées dans un champ propre au verticle `UserConnectionStoreVerticle`. Si ce verticle est
déployé plusieurs fois cela donnera donc lieu à des incohérences. En production il faudrait que ce verticle récupère les informations de la base de
données.


# Organisation du projet

Tout le code se trouve dans le package `com.ode.junior`.

Les classes sont réparties par domaine plutôt que par type.

Les classes des objets permettant le transit d'informations sur le bus se trouvent dans les packages contenant le
verticle qui crée les instances de ces classes.

# Build

To launch your tests:
```
./mvnw clean test
```

To package your application:
```
./mvnw clean package
```

To run your application:
```
./mvnw clean compile exec:java
```
