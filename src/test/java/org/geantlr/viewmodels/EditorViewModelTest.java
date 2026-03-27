package org.geantlr.viewmodels;

import org.geantlr.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EditorViewModelTest {
    private EditorViewModel viewModel;

    @BeforeEach
    void setUp() {
        // Passing nulls as we only test getErrorAt which doesn't use these services in its logic
        viewModel = new EditorViewModel(null, null, null, null, null, null);
    }

    @Test
    void testGetErrorAt() {
        SyntaxError error = new SyntaxError(1, 5, 3, "Test Error");
        viewModel.getErrors().add(error);

        assertEquals(error, viewModel.getErrorAt(1, 5));
        assertEquals(error, viewModel.getErrorAt(1, 6));
        assertEquals(error, viewModel.getErrorAt(1, 7));
        assertNull(viewModel.getErrorAt(1, 4));
        assertNull(viewModel.getErrorAt(1, 8));
        assertNull(viewModel.getErrorAt(2, 5));
    }

    @Test
    void testGetErrorAtZeroLength() {
        SyntaxError error = new SyntaxError(2, 10, 0, "Empty Error");
        viewModel.getErrors().add(error);

        // Should fallback to at least 1 character width for hover detection
        assertEquals(error, viewModel.getErrorAt(2, 10));
        assertNull(viewModel.getErrorAt(2, 11));
    }
}
