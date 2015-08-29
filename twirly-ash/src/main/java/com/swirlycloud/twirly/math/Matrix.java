/*******************************************************************************
 * Copyright (C) 2013, 2015 Swirly Cloud Limited. All rights reserved.
 *******************************************************************************/
package com.swirlycloud.twirly.math;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class Matrix {
    private final int rows;
    private final int cols;
    private final double[] data;

    protected Matrix(int rows, int cols, double[] data) {
        this.rows = rows;
        this.cols = cols;
        this.data = data;
        if (rows * cols != data.length) {
            throw new IllegalArgumentException("invalid matrix");
        }
    }

    protected Matrix(int rows, int cols) {
        this(rows, cols, new double[rows * cols]);
    }

    public static Matrix rowVector(double... values) {
        final double[] data = new double[values.length];
        for (int i = 0; i < values.length; ++i) {
            data[i] = values[i];
        }
        return new Matrix(1, values.length, data);
    }

    public static Matrix colVector(double... values) {
        final double[] data = new double[values.length];
        for (int i = 0; i < values.length; ++i) {
            data[i] = values[i];
        }
        return new Matrix(values.length, 1, data);
    }

    public static Matrix zero(int rows, int cols) {
        return new Matrix(rows, cols, new double[rows * cols]);
    }

    public static Matrix zero(int extent) {
        return zero(extent, extent);
    }

    public static Matrix scalar(int extent, double value) {
        final double[] data = new double[extent * extent];
        for (int i = 0; i < extent; ++i) {
            data[i * extent + i] = value;
        }
        return new Matrix(extent, extent, data);
    }

    public static Matrix identity(int extent) {
        return scalar(extent, 1.0);
    }

    public static Matrix diagonal(double... values) {
        final int extent = values.length;
        final double[] data = new double[extent * extent];
        for (int i = 0; i < extent; ++i) {
            data[i * extent + i] = values[i];
        }
        return new Matrix(extent, extent, data);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; ++i) {
            if (0 < i) {
                sb.append('\n');
            }
            for (int j = 0; j < cols; ++j) {
                if (0 < j) {
                    sb.append(' ');
                }
                sb.append(String.format("%.7g", data[i * cols + j]));
            }
        }
        return sb.toString();
    }

    public final Matrix transpose() {
        final Matrix m = Matrix.zero(cols, rows);
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                final int k = i * cols + j;
                m.data[k] = this.data[k];
            }
        }
        return m;
    }

    public final Matrix add(Matrix rhs) {
        if (rhs.rows != rows || rhs.cols != cols) {
            throw new IllegalArgumentException("invalid matrix");
        }
        final Matrix m = Matrix.zero(rows, cols);
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                final int k = i * cols + j;
                m.data[k] = data[k] + rhs.data[k];
            }
        }
        return m;
    }

    public final Matrix multiply(Matrix rhs) {
        if (rhs.rows != cols) {
            throw new IllegalArgumentException("invalid matrix");
        }
        final Matrix m = Matrix.zero(rows, rhs.cols);
        for (int i = 0; i < m.rows; ++i) {
            for (int j = 0; j < m.cols; ++j) {
                double sum = 0.0;
                for (int k = 0; k < cols; ++k) {
                    sum += data[i * cols + k] * rhs.data[k * m.cols + j];
                }
                m.data[i * m.cols + j] = sum;
            }
        }
        return m;
    }

    public final void clear() {
        final int len = data.length;
        for (int i = 0; i < len; ++i) {
            data[i] = 0;
        }
    }

    public final void setValue(int row, int col, double value) {
        // Assert only because this needs to be fast.
        assert 0 <= row && row < rows;
        assert 0 <= col && col < cols;
        data[row * cols + col] = value;
    }

    public final double getValue(int row, int col) {
        // Assert only because this needs to be fast.
        assert 0 <= row && row < rows;
        assert 0 <= col && col < cols;
        return data[row * cols + col];
    }

    public final int getCols() {
        return cols;
    }

    public final int getRows() {
        return rows;
    }
}
