import Testing
import Uuid

@Suite("Uuid Swift Export Suite")
struct UuidExportTests {
    @Test("Nil UUID and canonical parseStr roundtrip")
    func parseAndNilUuid() {
        let nilUuid = Uuid.Companion.shared.NIL
        #expect(nilUuid.toString() == "00000000-0000-0000-0000-000000000000")

        let parsed = Uuid.Companion.shared.parseStr(input: "67e55044-10b1-426f-9247-bb680e5fe0c8")
        #expect(parsed.toString() == "67e55044-10b1-426f-9247-bb680e5fe0c8")
    }
}
