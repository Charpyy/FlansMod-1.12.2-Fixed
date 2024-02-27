package com.flansmod.common;


import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SideOnly(Side.SERVER)
public class CSVWriter {

	private static final String CSV_FILE_PATH = "E:\\WW2 SERVEUR LE VRAI\\csv\\explode.csv";
	private static final long DELAY_MS = 100;

	public static void writeDataToCSV(String value) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(CSV_FILE_PATH, true))) {
			writer.write(value);
			writer.newLine();
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			executorService.schedule(() -> clearCSVContent(), DELAY_MS, TimeUnit.MILLISECONDS);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void clearCSVContent() {
		try {
			Files.write(Paths.get(CSV_FILE_PATH), "".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
