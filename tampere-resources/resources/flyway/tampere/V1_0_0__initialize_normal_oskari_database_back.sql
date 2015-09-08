DROP SEQUENCE IF EXISTS oskari_wfs_search_channel_seq CASCADE;
DROP TABLE IF EXISTS oskari_wfs_search_channels CASCADE;
DELETE FROM portti_bundle WHERE name='admin-wfs-search-channel';
DELETE FROM portti_view_bundle_seq WHERE bundle_id=(SELECT id FROM portti_bundle WHERE name='search-from-channels');
DELETE FROM portti_bundle WHERE name='search-from-channels';
UPDATE portti_view_bundle_seq SET config='{}' WHERE bundle_id=(SELECT id FROM portti_bundle WHERE name='search') AND view_id = 1;
UPDATE portti_view_bundle_seq SET config='{}' WHERE bundle_id=(SELECT id FROM portti_bundle WHERE name='publisher') AND view_id=1;