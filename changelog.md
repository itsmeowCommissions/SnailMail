# 1.1.0

- Ported to Architectury / Fabric, mod now depends on Architectury API
- Added ability for ops to bypass snailbox lock
- Add config options to alter behavior of op bypass
- Fix crash when summoning snailman via commands
- Fix warning in log about GlobalEntityTypeAttributes.put
- Fix bug where failure to return an envelope drops an open envelope instead of a closed one
- Fix bug where pressing Open Envelope stops working after the first time without re-opening the snailbox
- Fix bug where right clicking the snailbox with a block would place it on the client
- "Open Envelope" button now darkens when no envelope is present
- Fix Quark "Inventory Sorting" feature appearing on snailbox/envelope UI
- Added Mod Menu / Cloth config compat on Fabric
- Added Snailbox support for Fabric Transfer API on Fabric