package org.example.persistence;

import org.example.domain.Report;

import java.util.List;

public interface IReportRepository extends Repository<Long, Report> {

    public List<Report> getAllOfUser(Long user);
}
