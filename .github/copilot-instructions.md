# fp-uttak

Business-rule library for foreldrepenger uttak evaluation and calculation in relation to Folketrygdloven 14-9 through 14-16.

## Shared context

- Source of truth for shared domain, architecture, and conventions: `navikt/fp-context`
- Copilot Space: `navikt/TeamForeldrepenger`

## Repo-specific context

| Topic       | Details                                                          |
|-------------|------------------------------------------------------------------|
| Role        | Evaluates withdrawal periods and related uttaksutfall            |
| Consumers   | `fp-sak` module `uttak` foreldrepenger flow                      |
| Tech stack  | Java SemVer library using `fp-nare` rule framework               |
| API         | `FastsettePerioderRegelOrkestrering` with input / output objects |

- The orchestration entry point will iterate over time-sorted periods - evaluate each period and then break periods when no more stønadsdager available.
- The count of Trekkdager (stønadsdager consumed) is kept per activity: Employment, business, other 
- The core part of the output structure is list of periods, each with a result (granted, denied, manual evaluation) and a list of activities containing payout percentage and number of stønadsdager consumed.
- The resulting structure is used downstream in `fp-beregning-ytelse`, letters and statistics.
- Contains historical rule sets since 2019: evaluation paths selected based on input and rules. 

## Verification

- Verify behavior changes through `fp-sak` and relevant `navikt/fp-autotest` suites `fpsak` or `verdikjede` limited to foreldrepenger.
