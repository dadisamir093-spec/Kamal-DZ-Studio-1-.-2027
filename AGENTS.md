# KAMAL DZ STUDIO 2027 — Codex Review Rules

## Project Preservation

- Do not change the Android applicationId:
  `com.aistudio.kamaldzstudio.vwnxrq`
- Do not change the current signing identity or certificate.
- Do not rebuild, replace, or redesign the project from scratch.
- Preserve the existing application architecture, navigation, database, layouts, colors, and Arabic RTL behavior unless a requested change explicitly requires otherwise.
- Do not remove, rename, merge, or disable any of the seven existing studios:
  1. Generation Studio
  2. Character Studio
  3. Voice Studio
  4. Music Studio
  5. Editing/Montage Studio
  6. Library Studio
  7. Export Studio

## Studio 4 Protection

- Studio 4 (Music Studio) is considered a protected working area.
- Do not modify Studio 4 unless the requested change explicitly concerns Studio 4.
- Changes elsewhere must not introduce regressions into Studio 4.

## Security

- Never place API keys, provider secrets, private tokens, signing keys, or other credentials inside the Android APK or client-side source.
- Preserve Firebase App Check and do not disable, bypass, or weaken its security controls.
- Do not weaken authentication, authorization, quota, or server-side security controls merely to make a feature work.
- Flag any potential secret exposure or security regression as a high-priority review finding.

## Data and Compatibility

- Do not change the existing database schema, stored data format, or persistence behavior without explicitly identifying the compatibility impact.
- Preserve backward compatibility with the current installed application wherever possible.
- Before recommending a change, check whether it could break existing projects, saved user data, imports, exports, or installed-app updates.
- Do not change package identity or signing requirements because doing so can prevent normal updates to the existing installed application.

## Review Behavior

- Review changes before recommending merge.
- Do not automatically merge Pull Requests.
- Prioritize real functional regressions, security problems, data loss, compatibility problems, and breaking changes over stylistic issues.
- Do not propose large unrelated refactors.
- Keep fixes narrowly scoped to the requested task.
- If a requested change conflicts with these rules, explicitly report the conflict instead of silently changing the protected behavior.
- If there is insufficient evidence to determine whether a change is safe, report the uncertainty and request verification rather than assuming it is safe.

## Required Review Checks

For relevant changes, verify:

- applicationId remains unchanged.
- Signing/update compatibility is preserved.
- All seven studios remain present and functional.
- Studio 4 remains unchanged unless explicitly targeted.
- Arabic RTL behavior is preserved.
- Firebase App Check remains enabled and functional.
- No API keys or secrets are exposed in client code or APK resources.
- Existing database and saved-project compatibility is preserved.
- No unrelated files or features are unnecessarily modified.

## Merge Policy

Codex must only review and report findings.

Codex must not automatically merge Pull Requests or make irreversible project changes.
Final approval and merge decisions remain with the project owner.
