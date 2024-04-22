package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.OperationType
import com.example.p2p_project.repositories.dataTables.OperationTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component

class OperationTypeInitialize(val operationTypeRepository: OperationTypeRepository): CommandLineRunner {
    @Transactional
    override fun run(vararg args: String?) {
        if (operationTypeRepository.count() != 0L) return;
        val operationTypes: MutableList<OperationType> = mutableListOf()

        operationTypes.add(OperationType(-1, "Пополнение"))
        operationTypes.add(OperationType(-1, "Вывод"))
        operationTypes.add(OperationType(-1, "Первод на другой кошелек"))

        for (operationType in operationTypes) {
            operationTypeRepository.save(operationType)
        }
    }
}