package org.acerbulescu.models;

import lombok.*;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThreadInstance {
    Thread thread;

    ServerInstance instance;

    BufferedWriter writer;

    BufferedReader reader;

    BufferedReader errorReader;
}
