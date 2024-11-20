import com.example.p2p_stock.models.*
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime

object OrderSpecifications {

    fun hasStatus(statusName: String): Specification<Order> {
        return Specification { root, _, criteriaBuilder ->
            val statusJoin = root.join<Order, OrderStatus>("status")
            criteriaBuilder.equal(statusJoin.get<String>("name"), statusName)
        }
    }

    fun hasType(typeName: String): Specification<Order> {
        return Specification { root, _, criteriaBuilder ->
            val typeJoin = root.join<Order, OrderType>("type")
            criteriaBuilder.equal(typeJoin.get<String>("name"), typeName)
        }
    }

    fun hasCryptocurrencyCode(code: String): Specification<Order> {
        return Specification { root, _, criteriaBuilder ->
            val walletJoin = root.join<Order, Wallet>("wallet")
            val cryptoJoin = walletJoin.join<Wallet, Cryptocurrency>("cryptocurrency")
            criteriaBuilder.equal(cryptoJoin.get<String>("code"), code)
        }
    }

    fun createdAfter(date: LocalDateTime): Specification<Order> {
        return Specification { root, _, criteriaBuilder ->
            criteriaBuilder.greaterThan(root.get<LocalDateTime>("createdAt"), date)
        }
    }
}
