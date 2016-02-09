## Unreleased

**[compare](https://github.com/metosin/metosin-common/compare/0.1.0...master)**

- `metosin.dates`
    - Fix start/end-of-week with date-times on cljs
    - Replaced `today` and `now` with `date` and `date-time` zero-arity versions.
    - Support creating dates from good string representation
    - Add `to-string` to create good string representation for a object
- Imported more `java.jdbc` function to `metosin.jdbc`
- Added tests for Joda Time <-> JDBC conversions
- Fixed `LocalDate` <-> `java.sql.Date` conversion

## 0.1.0 (8.2.2016)

- Initial version