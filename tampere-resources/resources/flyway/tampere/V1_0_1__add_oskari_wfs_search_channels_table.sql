CREATE SEQUENCE oskari_wfs_search_channel_seq
  INCREMENT 1;

CREATE TABLE oskari_wfs_search_channels
(
  id integer NOT NULL DEFAULT nextval('oskari_wfs_search_channel_seq'::regclass),
  wfs_layer_id integer NOT NULL,
  topic character varying(4000) NOT NULL,
  description character varying(4000),
  params_for_search character varying(4000) NOT NULL,
  is_default boolean,
  is_address boolean,
  CONSTRAINT portti_wfs_search_channels_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE oskari_wfs_search_channels
  OWNER TO postgres;


