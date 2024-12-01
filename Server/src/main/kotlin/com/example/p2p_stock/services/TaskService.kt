package com.example.p2p_stock.services

import com.example.p2p_stock.dataclasses.TaskInfo
import com.example.p2p_stock.dataclasses.TaskPatchResultInfo
import com.example.p2p_stock.errors.IllegalAccessTaskException
import com.example.p2p_stock.errors.NotFoundTaskException
import com.example.p2p_stock.models.Deal
import com.example.p2p_stock.models.Task
import com.example.p2p_stock.models.User
import com.example.p2p_stock.repositories.TaskRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.time.times

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val dealService: DealService,
) {

    fun findAll(): List<Task>{
        return taskRepository.findAll().filter { it.deal?.status?.name =="Приостановлено: решение проблем" || it.deal?.status?.name =="Ожидание решения менеджера" }
    }

    fun findById(taskId: Long): Task{
        return taskRepository.findById(taskId).orElseThrow {
            NotFoundTaskException("Task with id: $taskId not found")
        }
    }

    fun save(task: Task): Task {
        task.updatedAt = LocalDateTime.now()
        return taskRepository.save(task)
    }

    fun delete(id: Long) = taskRepository.deleteById(id)

    fun takeInWork(user: User, task: Task): Task {
        dealService.managerTakeInWork(user, task.deal!!)
        task.manager = user

        return save(task)
    }

    fun denyDealTask(task: Task, result: TaskPatchResultInfo): Task {
        if(task.manager == null){
            throw IllegalAccessTaskException("Manager is null")
        }

        task.confirmation = false
        task.result = result.result
        val deal = task.deal!!

        dealService.managerReject(deal)

        return save(task)
    }

    fun acceptDealTask(task: Task, result: TaskPatchResultInfo): Task {
        if(task.manager == null){
            throw IllegalAccessTaskException("Manager is null")
        }

        task.confirmation = true
        task.result = result.result
        val deal = task.deal!!

        dealService.managerApprove(deal)

        return save(task)
    }

    fun taskToTaskInfo(task:Task): TaskInfo{
        return TaskInfo(
            id=task.id,
            dealInfo = dealService.dealToDealInfo(task.deal!!),
            managerLogin = task.manager?.login?:"",
            confirmation = task.confirmation,
            errorDescription = task.errorDescription,
            priorityName = task.priority!!.name,
            result = task.result,
            updatedAt = task.updatedAt.toString(),
        )
    }

}