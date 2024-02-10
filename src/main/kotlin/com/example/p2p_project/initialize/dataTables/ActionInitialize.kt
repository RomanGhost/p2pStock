package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.Action
import com.example.p2p_project.repositories.dataTables.ActionRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("actionInitialize")
class ActionInitialize(val actionRepository: ActionRepository):CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (actionRepository.count() != 0L) return;
        val actions:MutableList<Action> = mutableListOf()

        actions.add(Action(null, "Просмотр", "Просмотреть заявку"))
        actions.add(Action(null, "Изменить статус", "Измененение статус сделки: Подтвердить, отклонить"))
        actions.add(Action(null, "Редактирование сделки", "Изменить что-то внутри сделки"))
        actions.add(Action(null, "Запрет", "Доступ запрещен"))

        for(action in actions){
            actionRepository.save(action)
        }

    }
}