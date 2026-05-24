import XCTest
import Uuid
import ExportedKotlinPackages

// Swift Export bridge verification for the Kotlin → Swift Export → SPM →
// swift test pipeline. The smoke test (`testSwiftModuleLoads`) proves the
// swiftmodule, static archive, and SPM wiring are correct; the additional
// tests below exercise specific Kotlin entry points through the Swift
// bridge so a per-API regression in the bridge cannot pass silently.
//
// Swift accesses Kotlin companion objects as `Type.Companion.shared.member`
// per SWIFT_EXPORT_ROLLOUT.md gap #5. The bridge typealiases the Kotlin
// classes into the top-level Uuid Swift module, but the type ambiguity
// between the module name `Uuid` and the typealias `Uuid` forces every
// reference to disambiguate via the `ExportedKotlinPackages` namespace.
typealias KotlinUuid = ExportedKotlinPackages.io.github.kotlinmania.uuid.Uuid
typealias KotlinNonNilUuid = ExportedKotlinPackages.io.github.kotlinmania.uuid.NonNilUuid

final class UuidExportTests: XCTestCase {

    func testSwiftModuleLoads() throws {
        XCTAssertTrue(true, "Uuid swift module imported cleanly")
    }

    func testNilUuid() throws {
        let nilUuid = KotlinUuid.Companion.shared.`nil`()
        XCTAssertTrue(nilUuid.isNil())
        XCTAssertFalse(nilUuid.isMax())
        XCTAssertEqual(nilUuid.toString(), "00000000-0000-0000-0000-000000000000")
    }

    func testMaxUuid() throws {
        let maxUuid = KotlinUuid.Companion.shared.max()
        XCTAssertTrue(maxUuid.isMax())
        XCTAssertFalse(maxUuid.isNil())
        XCTAssertEqual(maxUuid.toString(), "ffffffff-ffff-ffff-ffff-ffffffffffff")
    }

    func testParseHyphenated() throws {
        let parsed = KotlinUuid.Companion.shared.parseStr(input: "550e8400-e29b-41d4-a716-446655440000")
        XCTAssertFalse(parsed.isNil())
        XCTAssertEqual(parsed.toString(), "550e8400-e29b-41d4-a716-446655440000")
    }

    func testNonNilUuidRejectsNil() throws {
        let nilUuid = KotlinUuid.Companion.shared.`nil`()
        let nonNilFromNil = KotlinNonNilUuid.Companion.shared.new(uuid: nilUuid)
        XCTAssertNil(nonNilFromNil)
    }

    func testNonNilUuidAcceptsRealUuid() throws {
        let uuid = KotlinUuid.Companion.shared.parseStr(input: "550e8400-e29b-41d4-a716-446655440000")
        let nonNil = KotlinNonNilUuid.Companion.shared.new(uuid: uuid)
        XCTAssertNotNil(nonNil)
        XCTAssertEqual(nonNil?.toString(), "550e8400-e29b-41d4-a716-446655440000")
    }
}
