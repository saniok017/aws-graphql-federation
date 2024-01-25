package com.github.arhor.aws.graphql.federation.users.data.entity.callback

import com.github.arhor.aws.graphql.federation.users.data.entity.OutboxEventEntity
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OutboxEventEntityCallback : BeforeConvertCallback<OutboxEventEntity> {

    override fun onBeforeConvert(aggregate: OutboxEventEntity): OutboxEventEntity {
        return if (aggregate.id == null) {
            aggregate.copy(id = UUID.randomUUID())
        } else {
            aggregate
        }
    }
}