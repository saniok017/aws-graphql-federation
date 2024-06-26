CREATE TABLE IF NOT EXISTS "user_representations"
(
    "id"       UUID NOT NULL PRIMARY KEY,
    "features" INT  NOT NULL
) WITH (OIDS = FALSE);

CREATE TABLE IF NOT EXISTS "posts"
(
    "id"                UUID         NOT NULL PRIMARY KEY,
    "user_id"           UUID         NULL,
    "title"             VARCHAR(512) NOT NULL,
    "content"           TEXT         NOT NULL,
    "version"           BIGINT       NOT NULL,
    "created_date_time" TIMESTAMP    NOT NULL,
    "updated_date_time" TIMESTAMP    NULL,

    CONSTRAINT "FK__posts__user_representations"
        FOREIGN KEY ("user_id")
            REFERENCES "user_representations" ("id")
            ON UPDATE CASCADE
            ON DELETE SET NULL
) WITH (OIDS = FALSE);

CREATE TABLE IF NOT EXISTS "tags"
(
    "id"   UUID        NOT NULL PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL UNIQUE
) WITH (OIDS = FALSE);

CREATE TABLE IF NOT EXISTS "posts_has_tags"
(
    "post_id" UUID NOT NULL,
    "tag_id"  UUID NOT NULL,

    CONSTRAINT "UQ__posts_has_tags__post_id__tag_id"
        UNIQUE ("post_id", "tag_id"),

    CONSTRAINT "FK__posts_has_tags__posts"
        FOREIGN KEY ("post_id")
            REFERENCES "posts" ("id")
            ON UPDATE CASCADE
            ON DELETE CASCADE,

    CONSTRAINT "FK__posts_has_tags__tags"
        FOREIGN KEY ("tag_id")
            REFERENCES "tags" ("id")
            ON UPDATE CASCADE
            ON DELETE CASCADE
) WITH (OIDS = FALSE);
