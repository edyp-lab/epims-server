package fr.edyp.epims.controller;

import fr.edyp.epims.database.dao.RobotRepository;
import fr.edyp.epims.database.entities.Robot;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.InstrumentJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api")
public class RobotController {

  private static final Logger LOGGER = LoggerFactory.getLogger(RobotController.class);
  @Autowired
  RobotRepository robotRepository;

  @GetMapping("/robots/{robotManufacturer}")
  public ResponseEntity<List<InstrumentJson>> getRobotInstruments(@PathVariable("robotManufacturer") String robotManufacturer) {

    List<InstrumentJson> instrumentJson = new ArrayList<>();
    List<Robot> robots = robotRepository.findByManufacturer(robotManufacturer);
    for(Robot r :robots) {
      instrumentJson.add(Converter.convert(r.getInstrument()));
    }
    return ControllerUtil.createResponseWithVersion(instrumentJson, InstrumentJson.class);
  }
}
