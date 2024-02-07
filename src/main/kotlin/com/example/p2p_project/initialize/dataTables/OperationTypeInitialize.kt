package com.example.p2p_project.initialize.dataTables

import com.example.p2p_project.models.dataTables.OperationType
import com.example.p2p_project.repositories.dataTables.OperationTypeRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class OperationTypeInitialize(val operationTypeRepository: OperationTypeRepository): CommandLineRunner {
    override fun run(vararg args: String?) {
        if (operationTypeRepository.count() != 0L) return;
        val operationTypes: MutableList<OperationType> = mutableListOf()

        operationTypes.add(OperationType(null, "Пополнение"))
        operationTypes.add(OperationType(null, "Вывод"))
        operationTypes.add(OperationType(null, "Первод на другой кошелек"))

        for (operationType in operationTypes) {
            operationTypeRepository.save(operationType)
        }
    }
}