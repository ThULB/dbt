# DBT [![Build Status](https://travis-ci.org/ThULB/dbt.svg?branch=main)](https://travis-ci.org/ThULB/dbt) [![Codacy Badge](https://app.codacy.com/project/badge/Grade/67f4010b74ca40c0898d2e60714dff5b)](https://www.codacy.com/gh/ThULB/dbt/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ThULB/dbt&amp;utm_campaign=Badge_Grade)
DBT (acronym for Digitale Bibliothek Thüringen) is an open source repository software that is build upon [MyCoRe](https://github.com/MyCoRe-Org/mycore), [MIR](https://github.com/MyCoRe-Org/mir) and [MODS](http://www.loc.gov/standards/mods/).

## Git-Style-Guide
For the moment see [agis-:Git-Style-Guide](https://github.com/agis-/git-style-guide) and use it with the following exceptions:
- Subject to commits is in form: `{JIRA-Ticket} {Ticket summary} {Commit summary}`, like `DBT-104 GIT-Migration add .travis.yml`
- Branch name to work on a ticket is in form: `issues/{JIRA-Ticket}-{Ticket Summary}`, like `issues/DBT-104-GIT-Migration`

---

## Installation instructions for developers

1. **Build JAR**:
    - Run:
      `mvn clean install`


2. **Deploy the JAR File**:
    - Move the generated JAR file (located in `dbt/target`) to the `lib` folder in your home directory.
      For example: `~/.mycore/dev-mir/lib`


3. **Update Solr Core Names**:
    - Change the Solr core names from `mir` and `mir-classifications` to `dbt` and `dbt-classifications` using your Solr Admin UI.
    - Restart your Solr server.


4. **Launch and Setup DBT**:
    - After starting your MIR, the DBT interface will launch.
    - Log in as the administrator (Superuser) and navigate in the User menu to "Edit Classifications".
    - Import all files from `src/main/resources/setup/classifications` for DBT to function correctly.
   - Reload main and classifications in the WebCLI if needed.

     `reload solr configuration main in core main`

     `reload solr configuration classification in core classification`
---
Stay tuned for more information. :bow:
