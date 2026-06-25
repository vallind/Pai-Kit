# Design / Tokens

This directory holds Figma Tokens Studio exports and Style Dictionary templates
for design-to-code token synchronization.

## Files

- `tokens.json` — exported from Figma Tokens Studio plugin
- `templates/` — Style Dictionary handlebars templates

## Workflow

1. Designer edits tokens in Figma → exports `tokens.json` here
2. Engineer runs `./scripts/sync-tokens.sh`
3. Script generates `ColorTokens.kt` etc. into `app/src/main/.../foundation/tokens/`
4. PR includes both `tokens.json` and generated `.kt` diff

## CI check

```yaml
- name: Verify tokens synced
  run: ./scripts/sync-tokens.sh --check
```

Fails if `tokens.json` was updated without regenerating `.kt` files.
