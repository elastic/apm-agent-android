# How to add licenses IDs

Open source licenses don't have common ids, only a name which tends to be written differently
depending on the dependency declaring it, for example:

- "The Apache License, Version 2.0"
  [(Ref)](https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib-common/1.7.20/kotlin-stdlib-common-1.7.20.pom)
- "The Apache Software License, Version 2.0"
  [(Ref)](https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.10.0/okhttp-4.10.0.pom)

Both being the same license, though provided with slightly different names. This causes issues for
automation tools.

Because of it, we're setting our own ids for the licenses we need for this project so that those can
be used by the NOTICE generation tasks to properly gather dependencies with the same license, as
well as providing their license body needed to be present in our NOTICE files.

## Steps

### 1. Checking if the licence is valid

Take a look first if the licence you're about to add is allowed
by [Elastic's license policy](https://github.com/elastic/open-source/blob/main/elastic-product-policy.md).

### 2. Creating the license ID

Within this project, `build-tools`, there's a file located at: `src/resources/licenses_ids.txt`
which contains all the licenses IDs needed for our NOTICE files generation. If the license you need
isn't listed there, you should add it in a new line with the following
format: `[THE_ID]|[THE LICENSE NAME]`, the id can be anything, without spaces, preferably all
lowercase, that makes sense based on the license name. Then, followed by a pipeline `|`, the license
name found on [this site preferably](https://opensource.org/licenses/alphabetical), only the full
name (without its abbreviation usually provided between parenthesis).

You should take a look at how the existing IDs are defined in order to get inspiration on how to add
a new one.

### 3. Creating the license body file

Each license's full body needs to be provided in the generated NOTICE file, therefore we are also
going to need to have it handy when it happens.

The license full body should be gotten preferably from the same website where the license name was
found, and it should be placed in this location: `src/resources/licenses/[THE_ID]`. The previously
created ID on step 1 must be the file name (without extensions, just the plain ID as defined before)
.

---

After those 2 steps are done, the `createNoticeFile` task should be able to use this newly added
license when handling dependencies that make use of it.
