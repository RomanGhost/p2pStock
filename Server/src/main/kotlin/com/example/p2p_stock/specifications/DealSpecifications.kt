import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.DealStatus
import com.example.p2p_stock.models.Order
import com.example.p2p_stock.models.User
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDate
import java.time.LocalDateTime

object DealSpecifications {

    fun hasStatus(statusName: String): Specification<Deal> {
        return Specification { root, _, criteriaBuilder ->
            val statusJoin = root.join<Deal, DealStatus>("status")
            criteriaBuilder.equal(statusJoin.get<String>("name"), statusName)
        }
    }

    fun createdAfter(date: LocalDateTime): Specification<Deal> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.greaterThan(root.get<LocalDateTime>("createdAt"), date)
        }
    }

    fun lastStatusChangeAfter(date: LocalDate): Specification<Deal> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.greaterThan(root.get("lastStatusChange"), date)
        }
    }

    fun userDeals(userId: Long): Specification<Deal> {
        return Specification { root, query, criteriaBuilder ->
            // Join с buyOrder и sellOrder
            val buyOrderJoin = root.join<Deal, Order>("buyOrder")
            val sellOrderJoin = root.join<Deal, Order>("sellOrder")

            // Условие: пользователь в одной из заказов
            criteriaBuilder.or(
                criteriaBuilder.equal(buyOrderJoin.get<User>("user").get<Long>("id"), userId),
                criteriaBuilder.equal(sellOrderJoin.get<User>("user").get<Long>("id"), userId)
            )
        }
    }
}
