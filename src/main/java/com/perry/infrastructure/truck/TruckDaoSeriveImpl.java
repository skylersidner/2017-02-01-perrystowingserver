package com.perry.infrastructure.truck;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.perry.domain.truck.Truck;

import rowmappers.TruckRowMapper;

@Named
public class TruckDaoSeriveImpl implements TruckDaoService {

	@Inject
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Override
	public List<Truck> getByIds(List<Long> truckIds) {
		String sql = "select * from trucks t where t.truck_id in (:truckIds)";
		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("truckIds", truckIds);

		List<Truck> truckList = namedParameterJdbcTemplate.query(sql, params, new TruckRowMapper());

		return truckList;
	}

	@Override
	public Truck create(Truck truck) {
		String sql = "INSERT INTO trucks(\r\n" + //
				"            driver_first_name, driver_last_name, status, insert_time, \r\n" + //
				"            update_time, insert_by, update_by)\r\n" + //
				"    VALUES (:driverFirstName, :driverLastName, :status, :insertTime,  \r\n" + //
				"            :updateTime, :insertBy, :updateBy);";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("driverFirstName", truck.getDriverFirstName());
		params.addValue("driverLastName", truck.getDriverLastName());
		params.addValue("status", truck.getTruckStatusType().getValue());
		params.addValue("insertTime", Instant.now().getEpochSecond());
		params.addValue("updateTime", Instant.now().getEpochSecond());
		params.addValue("insertBy", truck.getInsertBy());
		params.addValue("updateBy", truck.getUpdateBy());
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		namedParameterJdbcTemplate.update(sql, params, keyHolder);
		truck.setId((Long) keyHolder.getKeys().get("truck_id"));
		return truck;
	}

	@Override
	public List<Truck> getAll() {
		String sql = "select * from trucks order by insert_time asc";
		List<Truck> truckList = namedParameterJdbcTemplate.query(sql, new TruckRowMapper());

		String callCountSql = "SELECT truck_id, COUNT(call_id) as number_of_calls FROM calls GROUP BY truck_id";
		List<TruckCallNumber> truckCallNumberList = namedParameterJdbcTemplate.query(callCountSql, new TruckCallNumberRowMapper());
		for (TruckCallNumber truckCallNumber : truckCallNumberList) {
			for (Truck truck : truckList) {
				if (truck.getId() == truckCallNumber.getTruckId()) {
					truck.setNumberOfCalls(truckCallNumber.getNumberOfCalls());
					break;
				}
			}
		}
		return truckList;
	}


}
