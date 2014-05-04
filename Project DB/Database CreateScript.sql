SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `AndroidDB` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `AndroidDB` ;

-- -----------------------------------------------------
-- Table `AndroidDB`.`Measurement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `AndroidDB`.`Measurement` ;

CREATE TABLE IF NOT EXISTS `AndroidDB`.`Measurement` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `Time` DATETIME NOT NULL,
  `Avg` VARCHAR(45) NULL,
  `Duration` INT NOT NULL,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `AndroidDB`.`MeasureValues`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `AndroidDB`.`MeasureValues` ;

CREATE TABLE IF NOT EXISTS `AndroidDB`.`MeasureValues` (
  `ID` INT NOT NULL AUTO_INCREMENT,
  `X` VARCHAR(45) NOT NULL,
  `Y` VARCHAR(45) NOT NULL,
  `Z` VARCHAR(45) NOT NULL,
  `Measurement_ID` INT NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `fk_Values_Measurement_idx` (`Measurement_ID` ASC),
  CONSTRAINT `fk_Values_Measurement`
    FOREIGN KEY (`Measurement_ID`)
    REFERENCES `AndroidDB`.`Measurement` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
