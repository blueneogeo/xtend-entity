package nl.kii.entity.csv

import java.util.List
import nl.kii.entity.Entity
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import static extension nl.kii.entity.EntityExtensions.*
import static extension nl.kii.util.IterableExtensions.*

class CsvExtensions {
	def static csv(String csv, String... headers) {
		csv.csv(CSVFormat.DEFAULT.withHeader(headers))
	}
	
	def static csv(String csv) {
		csv.csv(CSVFormat.DEFAULT.withFirstRecordAsHeader)
	}
	
	def static csv(String csv, CSVFormat format) {
		csvParser(csv, format).toList
	}
	
	/** Returns a CSV parser as an iterator that can also be streamed */
	def static csvParser(String csv, CSVFormat format) {
		CSVParser.parse(csv, format).iterator
	}
	
	def static <T extends Entity> create(Class<T> type, CSVRecord csv) {
		csv.toMap.receive(type)
	}
	
	def static <T extends Entity> receive(CSVRecord csv, Class<T> type) {
		type.create(csv)
	}

	def static <T extends Entity> create(Class<T> type, List<CSVRecord> csv) {
		csv.map [ receive(type) ].list
	}
	
	def static <T extends Entity> receiveList(List<CSVRecord> csv, Class<T> type) {
		type.create(csv)
	}
	
}