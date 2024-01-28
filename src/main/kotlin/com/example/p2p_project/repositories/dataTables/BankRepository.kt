package com.example.p2p_project.repositories.dataTables

import com.example.p2p_project.models.dataTables.Bank
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BankRepository : JpaRepository<Bank, Long>
