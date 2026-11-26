# Contributing
We appreciate any pull requests for changes you may have in mind! Here are some tips and instructions you should follow before we can accept your PR.

* Add a changeset file which explains the changes contained in the PR.

  Create a file such as `.changeset/my-change.md`:
  ```
  ---
  "server-sdk-kotlin": minor
  ---

  Add X to Y.
  ```

  Use `patch` for a fix and `minor` for an addition or a breaking change.
  The summary is one sentence for users of the SDK. With
  [verwalter](https://github.com/livekit/verwalter) installed,
  `verwalter changeset` writes the file. If you forget it, the changeset
  check comments on the PR with a link to a prefilled file.

* Format your code using `./gradlew spotlessApply`.

* On your first pull request, the CLA Assistant bot will give you a link to sign this project's Contributor License Agreement, required to add your code to the repository. This license is non-optional and we cannot accept any PRs from contributors who have not signed the CLA.

