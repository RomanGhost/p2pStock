package com.example.p2p_stock.controllers

import com.example.p2p_stock.configs.MyUserDetails
import com.example.p2p_stock.dataclasses.DealInfo
import com.example.p2p_stock.dataclasses.TaskInfo
import com.example.p2p_stock.dataclasses.TaskPatchResultInfo
import com.example.p2p_stock.errors.UserException
import com.example.p2p_stock.services.TaskService
import com.example.p2p_stock.services.UserService
import com.example.p2p_stock.socket_handler.WebSocketHandler
import jakarta.websocket.server.PathParam
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("\${application.info.apiLink}/moderation/task")
class TaskController(
    private val taskService: TaskService,
    private val userService: UserService,
    private val dealWebSocketHandler: WebSocketHandler<DealInfo>
) {
    @GetMapping("/get/all/actual")
    fun getAllTasks(): List<TaskInfo> {
        return taskService.findAll().map{taskService.taskToTaskInfo(it)}
    }

    @PatchMapping("/take_in_word/{taskId}")
    fun takeInWorkTask(
        @AuthenticationPrincipal userDetails: MyUserDetails,
        @PathVariable("taskId") taskId:Long
    ): TaskInfo {
        val user = userService.findByEmail(userDetails.username) ?: throw UserException("Пользователь не найден")
        val task = taskService.findById(taskId)

        val taskInWork = taskService.takeInWork(user, task)
        return taskService.taskToTaskInfo(taskInWork)
    }

    @PatchMapping("/accept/{taskId}")
    fun acceptTask(
        @AuthenticationPrincipal userDetails: MyUserDetails,
        @PathVariable("taskId") taskId:Long,
        @RequestBody resultText: TaskPatchResultInfo
    ): TaskInfo {
        val task = taskService.findById(taskId)

        val taskInWork = taskService.acceptDealTask(task, resultText)

        val taskInfo = taskService.taskToTaskInfo(taskInWork)
        dealWebSocketHandler.sendUpdateToAll(taskInfo.dealInfo)
        return taskInfo
    }

    @PatchMapping("/deny/{taskId}")
    fun denyTask(
        @AuthenticationPrincipal userDetails: MyUserDetails,
        @PathVariable("taskId") taskId:Long,
        @RequestBody resultText: TaskPatchResultInfo
    ): TaskInfo {
        val task = taskService.findById(taskId)

        val taskInWork = taskService.denyDealTask(task, resultText)

        val taskInfo = taskService.taskToTaskInfo(taskInWork)
        dealWebSocketHandler.sendUpdateToAll(taskInfo.dealInfo)
        return taskInfo
    }


}