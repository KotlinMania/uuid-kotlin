# uuid-kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fuuid--kotlin-blue.svg)](https://github.com/KotlinMania/uuid-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/uuid-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/uuid-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/uuid-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/uuid-kotlin/actions)

**uuid-kotlin** is a Kotlin Multiplatform library for generating, parsing, formatting, and inspecting 128-bit Universally Unique Identifiers ([RFC 9562](https://www.ietf.org/rfc/rfc9562.html)), built by **Sydney Renee (`@sydneyrenee`)**, **The Solace Project**, and **[KotlinMania](https://github.com/KotlinMania/uuid-kotlin)**.

It is a faithful, idiomatic Kotlin Multiplatform transliteration of Rust's [`uuid`](https://github.com/uuid-rs/uuid) library—supporting UUID versions 1, 3, 4, 5, 6, 7, and 8, standard string and binary representations, `NonNilUuid`, low-level `Builder` construction, timestamp/clock-sequence contexts, `serde-kotlin` serialization, and Apple Swift Export (`Uuid`), with zero Java stdlib dependencies.

---

## Installation

### Kotlin Multiplatform

Add the dependency to `commonMain` in your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.kotlinmania:uuid-kotlin:0.1.3")
        }
    }
}
```

### JVM / Android

```kotlin
dependencies {
    implementation("io.github.kotlinmania:uuid-kotlin:0.1.3")
}
```

---

## Quick Start

```kotlin
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.Version
import io.github.kotlinmania.uuid.newV4
import io.github.kotlinmania.uuid.nowV7

// Generate a random (v4) UUID
val randomId = Uuid.newV4()

// Generate a time-ordered (v7) UUID using the current Unix timestamp
val sortableId = Uuid.nowV7()

// Parse a UUID string and inspect its version
val parsed = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")
check(parsed.getVersion() == Version.Random)
```

---

## Usage

All core types live in package `io.github.kotlinmania.uuid`.

### Generating UUIDs (v1, v3, v4, v5, v6, v7, v8)

```kotlin
import io.github.kotlinmania.uuid.Context
import io.github.kotlinmania.uuid.ContextV7
import io.github.kotlinmania.uuid.Timestamp
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.newV1
import io.github.kotlinmania.uuid.newV3
import io.github.kotlinmania.uuid.newV4
import io.github.kotlinmania.uuid.newV5
import io.github.kotlinmania.uuid.newV6
import io.github.kotlinmania.uuid.newV7
import io.github.kotlinmania.uuid.newV8
import io.github.kotlinmania.uuid.nowV1
import io.github.kotlinmania.uuid.nowV6
import io.github.kotlinmania.uuid.nowV7

// Version 4: random UUID backed by OS entropy via getrandom-kotlin
val v4 = Uuid.newV4()

// Version 7: Unix-epoch timestamp + random/monotonic counter (recommended for sortable IDs)
val v7 = Uuid.nowV7()

// Version 7 with an explicit ContextV7 (supports sub-millisecond precision & timestamp adjustment)
val v7Context = ContextV7().withAdditionalPrecision()
val v7WithContext = Uuid.newV7(Timestamp.now(v7Context))

// Version 3 (MD5) & Version 5 (SHA-1): deterministic namespace + name UUIDs
// Predefined namespaces: NAMESPACE_DNS, NAMESPACE_URL, NAMESPACE_OID, NAMESPACE_X500
val v3 = Uuid.newV3(Uuid.NAMESPACE_DNS, "example.org".encodeToByteArray())
// 04738bdf-b25a-3829-a801-b21a1d25095b

val v5 = Uuid.newV5(Uuid.NAMESPACE_DNS, "example.org".encodeToByteArray())
// aad03681-8b63-5304-89e0-8ca8f49461b5

// Version 1 & Version 6: Gregorian timestamp + 6-byte node ID
val nodeId = byteArrayOf(1, 2, 3, 4, 5, 6)
val v1 = Uuid.nowV1(nodeId)
val v6 = Uuid.nowV6(nodeId)

// Or supply an explicit Timestamp + ClockSequence (Context or NoContext)
val v1Explicit = Uuid.newV1(Timestamp.now(Context()), nodeId)
val v6Explicit = Uuid.newV6(Timestamp.now(Context()), nodeId)

// Version 8: custom 16-byte payload with RFC 9562 version and variant bits set
val v8 = Uuid.newV8(ByteArray(16) { it.toByte() })
```

### Parsing Strings

`Uuid.parseStr` accepts hyphenated, simple (32 hex digits), `{braced}`, and `urn:uuid:` strings, throwing `io.github.kotlinmania.uuid.Error` on invalid input. Use `Uuid.tryParse` or `Uuid.tryParseAscii` when you prefer a Kotlin `Result<Uuid>`.

```kotlin
import io.github.kotlinmania.uuid.Macros
import io.github.kotlinmania.uuid.Uuid

val fromHyphenated = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")
val fromSimple = Uuid.parseStr("67e5504410b1426f9247bb680e5fe0c8")
val fromUrn = Uuid.parseStr("urn:uuid:67e55044-10b1-426f-9247-bb680e5fe0c8")
val fromBraced = Uuid.parseStr("{67e55044-10b1-426f-9247-bb680e5fe0c8}")

// Inline literal helper
val literal = Macros.uuid("67e55044-10b1-426f-9247-bb680e5fe0c8")

// Non-throwing Result<Uuid> variants
val parsedResult: Result<Uuid> = Uuid.tryParse("67e55044-10b1-426f-9247-bb680e5fe0c8")
val asciiResult: Result<Uuid> =
    Uuid.tryParseAscii("67e55044-10b1-426f-9247-bb680e5fe0c8".encodeToByteArray())
```

### Formatting (`Hyphenated`, `Simple`, `Urn`, `Braced`)

`Uuid.toString()` formats as a lowercase hyphenated string. Dedicated formatter adapters (`Hyphenated`, `Simple`, `Urn`, `Braced`) provide lowercase/uppercase buffer encoding and format-strict parsing.

```kotlin
import io.github.kotlinmania.uuid.Hyphenated
import io.github.kotlinmania.uuid.Simple
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.braced
import io.github.kotlinmania.uuid.hyphenated
import io.github.kotlinmania.uuid.simple
import io.github.kotlinmania.uuid.urn

val uuid = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")

uuid.toString()              // "67e55044-10b1-426f-9247-bb680e5fe0c8"
uuid.hyphenated().toString() // "67e55044-10b1-426f-9247-bb680e5fe0c8"
uuid.simple().toString()     // "67e5504410b1426f9247bb680e5fe0c8"
uuid.urn().toString()        // "urn:uuid:67e55044-10b1-426f-9247-bb680e5fe0c8"
uuid.braced().toString()     // "{67e55044-10b1-426f-9247-bb680e5fe0c8}"

// Reusable 45-byte buffer (Urn.LENGTH) for lower/upper encoding
val buffer = Uuid.encodeBuffer()
val upperHyphenated = uuid.hyphenated().encodeUpper(buffer)
// "67E55044-10B1-426F-9247-BB680E5FE0C8"
val lowerSimple = uuid.simple().encodeLower(buffer)
// "67e5504410b1426f9247bb680e5fe0c8"

// Strict single-format parsing
val parsedHyphenated: Result<Hyphenated> = Hyphenated.parse("67e55044-10b1-426f-9247-bb680e5fe0c8")
val parsedSimple: Result<Simple> = Simple.parse("67e5504410b1426f9247bb680e5fe0c8")
```

### Inspecting Version, Variant, Timestamp, Bytes & Fields

```kotlin
import io.github.kotlinmania.uuid.Timestamp
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.UuidFields
import io.github.kotlinmania.uuid.Variant
import io.github.kotlinmania.uuid.Version
import io.github.kotlinmania.uuid.nowV7

val uuid = Uuid.parseStr("67e55044-10b1-426f-9247-bb680e5fe0c8")

val version: Version? = uuid.getVersion()  // Version.Random
val versionNum: Int = uuid.getVersionNum() // 4
val variant: Variant = uuid.getVariant()   // Variant.RFC4122

// Big-endian and little-endian byte arrays
val bytes: ByteArray = uuid.asBytes()
val leBytes: ByteArray = uuid.toBytesLe()
val fromBytes = Uuid.fromBytes(bytes)
val fromLeBytes = Uuid.fromBytesLe(leBytes)

// Four-field decomposition (d1: UInt, d2: UShort, d3: UShort, d4: ByteArray)
val fields: UuidFields = uuid.asFields()
val fromFields = Uuid.fromFields(fields.d1, fields.d2, fields.d3, fields.d4)

// 128-bit unsigned word pair (high: ULong, low: ULong)
val (high, low) = uuid.asU128()
val fromU128 = Uuid.fromU128(high, low)

// Timestamp extraction for v1, v6, and v7 UUIDs (returns null for other versions)
val v7 = Uuid.nowV7()
val ts: Timestamp? = v7.getTimestamp()
val unixTime: Pair<ULong, UInt>? = ts?.toUnix() // (seconds, subsecNanos)
```

### Nil, Max, `NonNilUuid`, and `Builder`

```kotlin
import io.github.kotlinmania.uuid.Builder
import io.github.kotlinmania.uuid.NonNilUuid
import io.github.kotlinmania.uuid.Uuid
import io.github.kotlinmania.uuid.Variant
import io.github.kotlinmania.uuid.Version
import io.github.kotlinmania.uuid.newV4

// Sentinel constants and predicates
val nilId = Uuid.NIL // 00000000-0000-0000-0000-000000000000 (also Uuid.nil())
val maxId = Uuid.MAX // ffffffff-ffff-ffff-ffff-ffffffffffff (also Uuid.max())
check(nilId.isNil())
check(maxId.isMax())

// NonNilUuid guarantees a UUID is never nil
val maybeNonNil: NonNilUuid? = NonNilUuid.new(Uuid.newV4())
val nilRejected: NonNilUuid? = NonNilUuid.new(Uuid.NIL) // null
val nonNil: NonNilUuid = NonNilUuid.tryFrom(Uuid.newV4()).getOrThrow()
val innerUuid: Uuid = nonNil.get()

// Low-level construction and variant/version bit manipulation via Builder
val built: Uuid =
    Builder
        .fromBytes(ByteArray(16))
        .withVariant(Variant.RFC4122)
        .withVersion(Version.Random)
        .intoUuid()
```

### Serialization & Ecosystem Support

`io.github.kotlinmania.uuid.external` provides adapters matching upstream feature modules:

- **`serde-kotlin`**: `Uuid`, `NonNilUuid`, `Hyphenated`, `Simple`, `Urn`, and `Braced` implement `Serialize` and companion `Deserialize<T>`. `SerdeSupport` adds explicit `Compact` (16-byte tuple/bytes), `SimpleFormat`, `BracedFormat`, and `UrnFormat` codecs.
- **`BorshSupport`**, **`ArbitrarySupport`**, **`SlogSupport`**, and **`External`**: lightweight helpers for Borsh byte conversion, arbitrary generation, and log formatting.

### Swift Export

On Apple platforms, `uuid-kotlin` exports the `Uuid` Swift module directly via Kotlin Multiplatform Swift Export:

```swift
import Uuid

let nilUuid = Uuid.Companion.shared.NIL
let parsed = Uuid.Companion.shared.parseStr(input: "67e55044-10b1-426f-9247-bb680e5fe0c8")
```

---

## Supported Targets

`uuid-kotlin` builds across 17 Kotlin Multiplatform targets:

| Platform Family | Targets |
|---|---|
| **JVM** | `jvm` (Java 21 toolchain) |
| **Android** | `android` (API 24+ / `minSdk = 24`, `compileSdk = 34`) |
| **Android Native** | `androidNativeArm64`, `androidNativeX64` |
| **Apple** | `macosArm64`, `iosArm64`, `iosSimulatorArm64`, `tvosArm64`, `tvosSimulatorArm64`, `watchosDeviceArm64`, `watchosSimulatorArm64` (XCFramework & Swift Export module `Uuid`) |
| **Linux** | `linuxX64`, `linuxArm64` |
| **Windows** | `mingwX64` |
| **Web / Wasm** | `js` (Browser & Node.js), `wasmJs` (Browser & Node.js), `wasmWasi` (Node.js) |

---

## Building & Testing

```bash
# Build all enabled Kotlin Multiplatform targets, XCFrameworks, and checks
./gradlew build

# Run test suites across enabled host targets and the Swift Export test harness
./gradlew test
```

---

## Acknowledgments

Thanks to the [`uuid-rs/uuid`](https://github.com/uuid-rs/uuid) maintainers and contributors—including Ashley Mannix, Christopher Armstrong, Dylan DPC, Hunar Roop Kahlon, and The Rust Project Developers—for the original Rust implementation, test vectors, and API design. `uuid-kotlin` is a faithful Kotlin Multiplatform transliteration port of their work.

### References

- [RFC 9562: Universally Unique IDentifiers (UUID)](https://www.ietf.org/rfc/rfc9562.html)
- [`uuid-rs/uuid` upstream repository](https://github.com/uuid-rs/uuid)
- [`uuid` crate documentation](https://docs.rs/uuid)

---

## License

Kotlin port: Copyright (c) 2026 Sydney Renee (`@sydneyrenee`) and The Solace Project / KotlinMania.  
Original Rust `uuid` work: Copyright (c) 2014 The Rust Project Developers; Copyright (c) 2018 Ashley Mannix, Christopher Armstrong, Dylan DPC, Hunar Roop Kahlon.

Licensed under either of:

- Apache License, Version 2.0 ([LICENSE](LICENSE), [LICENSE-APACHE](LICENSE-APACHE), or https://www.apache.org/licenses/LICENSE-2.0)
- MIT License ([LICENSE-MIT](LICENSE-MIT), or https://opensource.org/licenses/MIT)

at your option.
