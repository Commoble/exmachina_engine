Ex Machina Engine is a power API mod for the Neoforge modloader that uses JSON data to assign power properties to blocks.

This API is still in alpha and both the mod, its API, this readme, and the documentation are still under construction, and any evidence that the mod actually works is therefore purely coincidental.

## Dependency Setup

Mod creators can use the API in their gradle projects by adding this information to their build.gradle:

```
repositories {
	maven { url "https://cubicinterpolation.net/maven/" }
}

dependencies {
	implementation fg.deobf("commoble.exmachina_engine:exmachina_engine-${exmachina_engine_mc_version}:${exmachina_engine_version}")
	// To include exmachina engine with your mod via jarjar, see: https://forge.gemwire.uk/wiki/Jar-in-Jar for full jarjar setup details
	// jarJar(group: "commoble.exmachina_engine", name: "exmachina_engine-${exmachina_engine_mc_version}", version: "[${exmachina_engine_version}, ${exmachina_engine_next_version})")
}
```
where
* `${exmachina_engine_mc_version}` is the version of minecraft Ex Machina Engine was compiled against
* `${exmachina_engine_version}"` is the four-number Ex Machina version
* `${exmachina_engine_next_version}` (recommended) is the next api-breaking version of Ex Machina (see version spec below)

See the Maven file list for available versions

## Version Semantics
Ex Machina Engine uses version numbers A.B.C.D, where
* Increments to A indicate breaks in save compatibility. Users will need to create a new world if they update Ex Machina Engine.
* Increments to B indicate breaking changes in APIs (including reading of datapack files). Userdevs may need to update their mod to be compatible with the new version of Ex Machina Engine.
* Increments to C indicate breaking changes to netcode. Client players with Ex Machina Engine will need to install a newer version to play on servers with newer versions. Currently, Ex Machina Engine only runs on the server, but this may not last forever.
* Increments to D indicate nothing broke.

## JSON APIs
Circuit components can be assigned to blocks by defining component jsons at `data/<block_namespace>/exmachina/circuit_component/<block_id>.json`:
```json5
{
	"connector": {}, // connector object, defaults to an invalid component if not present
	"static_load": 1.0, // double or static property object, ohms, defaults to 0 if not present
	"static_source": 1.0, // double or static property object, volts, defaults to 0 if not present
	"dynamic_load": {}, // dynamic property object, optional
	"dynamic_source": {}, // dynamic property object, optional
}
```
Some builtin types are included, a block can be made into a simple wire block as such:
```json5
{
	"connector":{"type": "exmachina:all_directions"},
	"static_load": 0.1
}
```

Blocks that can connect to each other will form a circuit, if the circuit has at least one static load provider and any source provider.

## Java APIs
Public APIs and libraries are available in the `commoble.exmachina.engine.api` package and any subpackages thereof. Points of interest:
* `CircuitManager.get(ServerLevel)` retrieves the circuit manager for a level, which can query level positions for a circuit and power generation/consumption
* `ExMachinaRegistries` contains registry keys for registering json codecs
