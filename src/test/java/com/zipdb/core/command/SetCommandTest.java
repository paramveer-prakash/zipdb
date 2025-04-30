package com.zipdb.core.command;

import com.zipdb.core.DataStore;
import com.zipdb.core.datatype.StringType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SetCommandTest {

    private DataStore dataStore;
    private SetCommand setCommand;

    @BeforeEach
    void setUp() {
        // Create a mock DataStore to test SetCommand without relying on the actual DataStore implementation.
        dataStore = mock(DataStore.class);
        setCommand = new SetCommand(dataStore);
    }

    @Test
    void testExecuteValidSetCommand() {
        // Arrange
        String[] args = {"key1", "value1"};

        // Act
        String response = setCommand.execute(args);

        // Assert
        assertEquals("OK", response);  // Check that the response is OK
        verify(dataStore, times(1)).set(eq("key1"), eq(new StringType("value1")));  // Ensure that the `set` method was called once with correct args
    }

    @Test
    void testExecuteInvalidSetCommand_FewArguments() {
        // Arrange
        String[] args = {"key1"};

        // Act
        String response = setCommand.execute(args);

        // Assert
        assertEquals("ERR wrong number of arguments for 'set' command", response);  // Ensure the error message is returned
        verify(dataStore, times(0)).set(anyString(), any());  // Ensure the `set` method was not called
    }

    @Test
    void testExecuteInvalidSetCommand_NoArguments() {
        // Arrange
        String[] args = {};

        // Act
        String response = setCommand.execute(args);

        // Assert
        assertEquals("ERR wrong number of arguments for 'set' command", response);  // Ensure the error message is returned
        verify(dataStore, times(0)).set(anyString(), any());  // Ensure the `set` method was not called
    }
}
