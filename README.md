# uuid-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fuuid--kotlin-blue.svg)](https://github.com/KotlinMania/uuid-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/uuid-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/uuid-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/uuid-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/uuid-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`uuid-rs/uuid`](https://github.com/uuid-rs/uuid).

**Original Project:** This port is based on [`uuid-rs/uuid`](https://github.com/uuid-rs/uuid). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `uuid-rs/uuid`

> The text below is reproduced and lightly edited from [`https://github.com/uuid-rs/uuid`](https://github.com/uuid-rs/uuid). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## `uuid`

[![Latest Version](https://img.shields.io/crates/v/uuid.svg)](https://crates.io/crates/uuid)
[![Continuous integration](https://github.com/uuid-rs/uuid/actions/workflows/ci.yml/badge.svg)](https://github.com/uuid-rs/uuid/actions/workflows/ci.yml)

Here's an example of a UUID:

```text
67e55044-10b1-426f-9247-bb680e5fe0c8
```

A UUID is a unique 128-bit value, stored as 16 octets, and regularly
formatted as a hex string in five groups. UUIDs are used to assign unique
identifiers to entities without requiring a central allocating authority.

They are particularly useful in distributed systems, though can be used in
disparate areas, such as databases and network protocols.  Typically a UUID
is displayed in a readable string form as a sequence of hexadecimal digits,
separated into groups by hyphens.

The uniqueness property is not strictly guaranteed, however for all
practical purposes, it can be assumed that an unintentional collision would
be extremely unlikely.

## Getting started

Add the following to your `Cargo.toml`:

```toml
[dependencies.uuid]
version = "1.23.1"
# Lets you generate random UUIDs
features = [
    "v4",
]
```

When you want a UUID, you can generate one:

```rust
use uuid::Uuid;

let id = Uuid::new_v4();
```

If you have a UUID value, you can use its string literal form inline:

```rust
use uuid::{uuid, Uuid};

const ID: Uuid = uuid!("67e55044-10b1-426f-9247-bb680e5fe0c8");
```

You can also parse UUIDs without needing any crate features:

```rust
use uuid::{Uuid, Version};

let my_uuid = Uuid::parse_str("67e55044-10b1-426f-9247-bb680e5fe0c8")?;

assert_eq!(Some(Version::Random), my_uuid.get_version());
```

If you'd like to parse UUIDs _really_ fast, check out the [`uuid-simd`](https://github.com/nugine/uuid-simd)
library.

For more details on using `uuid`, [see the library documentation](https://docs.rs/uuid/1.23.1/uuid).

## References

* [`uuid` library docs](https://docs.rs/uuid/1.23.1/uuid).
* [Wikipedia: Universally Unique Identifier](http://en.wikipedia.org/wiki/Universally_unique_identifier).
* [RFC 9562: Universally Unique IDentifiers (UUID)](https://www.ietf.org/rfc/rfc9562.html).

---
# License

Licensed under either of

* Apache License, Version 2.0, (LICENSE-APACHE or https://www.apache.org/licenses/LICENSE-2.0)
* MIT license (LICENSE-MIT or https://opensource.org/licenses/MIT)

at your option.

## Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted
for inclusion in the work by you, as defined in the Apache-2.0 license, shall
be dual licensed as above, without any additional terms or conditions.

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:uuid-kotlin:0.1.1")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same Apache-2.0 license as the upstream [`uuid-rs/uuid`](https://github.com/uuid-rs/uuid). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the uuid authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`uuid-rs/uuid`](https://github.com/uuid-rs/uuid) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
