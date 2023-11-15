package jp.tengopapa.algo.solvers;

import java.util.*;
import java.util.stream.Collectors;

public class PolishNotation {
    public List<ConversionStep> convertToInfixNotation(String input) {
        final List<ConversionStep> steps = new ArrayList<>();
        final List<EquationFrame> parsedInput = parseString(input, false);

        List<EquationFrame> output = new ArrayList<>();
        Stack<EquationFrame> v = new Stack<>();

        int idx = 0;
        for(EquationFrame frame : parsedInput) {
            if(frame.isOperator()) {
                Operators op = frame.asOperator();

                if(op.getNumOperands() == 0) {
                    continue;
                }

                EquationFrame b = v.pop();
                EquationFrame a;

                if(op.getNumOperands() == 2) {
                    a = v.pop();

                    EquationFrame nf = new EquationFrame(false, String.format("(%s%c%s)", a.asOperand(), op.getOperatorCharacter(), b.asOperand()));
                    v.push(nf);
                } else {
                    EquationFrame nf = new EquationFrame(false, String.format("(%c%s)", op.getOperatorCharacter(), b.asOperand()));
                    v.push(nf);
                }
            } else {
                v.push(frame);
            }

            steps.add(new ConversionStep(takeSnapshot(v), new ArrayList<>(output), idx++ >= parsedInput.size() - 1 ? null : parsedInput.get(idx)));
        }

        while(!v.isEmpty()) {
            output.add(v.pop());
        }

        steps.add(new ConversionStep(takeSnapshot(v), new ArrayList<>(output), null));

        return steps;
    }

    public double evaluateExpression(String input, Map<String, Double> assignments) {
        final List<EquationFrame> parsedInput = parseString(input, false);

        Stack<Double> v = new Stack<>();

        for(EquationFrame frame : parsedInput) {
            if(frame.isOperator()) {
                Operators op = frame.asOperator();

                if(op.getNumOperands() == 0) {
                    continue;
                }

                double b = v.pop();
                double a = 0d;

                if(op.getNumOperands() == 2) {
                    a = v.pop();
                }

                v.push(frame.asOperator().impl.evaluate(a, b));
            } else {
                String operandData = frame.asOperand();

                if(assignments.containsKey(operandData)) {
                    v.push(assignments.get(operandData));
                } else {
                    v.push(Double.parseDouble(operandData));
                }
            }
        }

        return v.pop();
    }

    public List<ConversionStep> convertToPolishNotation(String input) {
        final List<ConversionStep> conversionSteps = new ArrayList<>();
        List<EquationFrame> parsedInput = parseString(input, true);

        List<EquationFrame> out = new ArrayList<>();
        Stack<EquationFrame> v = new Stack<>();

        int idx = 0;
        for(EquationFrame x : parsedInput) {
            if(x.isOperator()) {
                Operators op = x.asOperator();

                if(op == Operators.OPEN_BRACKET) {
                    v.push(x);
                } else if(op == Operators.CLOSE_BRACKET) {
                    while(v.peek().asOperator() != Operators.OPEN_BRACKET) {
                        out.add(v.pop());
                    }

                    v.pop();
                } else if(op.getDirection() == Operators.Dir.BJ) {
                    while(!v.isEmpty() && v.peek().asOperator() != Operators.OPEN_BRACKET && v.peek().asOperator().getPrecedence() >= op.getPrecedence()) {
                        out.add(v.pop());
                    }

                    v.push(x);
                } else if(op.getDirection() == Operators.Dir.JB) {
                    while(!v.isEmpty() && v.peek().asOperator() != Operators.OPEN_BRACKET && v.peek().asOperator().getPrecedence() > op.getPrecedence()) {
                        out.add(v.pop());
                    }

                    v.push(x);
                }
            } else {
                out.add(x);
            }

            conversionSteps.add(new ConversionStep(takeSnapshot(v), new ArrayList<>(out), idx++ >= parsedInput.size() - 1 ? null : parsedInput.get(idx)));
        }

        while(!v.isEmpty()) {
            out.add(v.pop());
        }

        conversionSteps.add(new ConversionStep(takeSnapshot(v), new ArrayList<>(out), null));
        return conversionSteps;
    }

    public String convertToPolishNotation0(String input) {
        List<ConversionStep> steps = convertToPolishNotation(input);
        return stringifyFrames(steps.get(steps.size() - 1).getOutputSnapshot());
    }

    public String stringifyFrames(List<EquationFrame> frames) {
        if(frames.size() == 0) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();

        for(EquationFrame frame : frames) {
            stringBuilder.append(frame.toString()).append(',');
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private List<EquationFrame> takeSnapshot(Stack<EquationFrame> stack) {
        if(stack.isEmpty()) {
            return new ArrayList<>();
        }

        EquationFrame[] frames = stack.toArray(new EquationFrame[0]);
        return Arrays.stream(frames).collect(Collectors.toList());
    }

    private List<EquationFrame> parseString(String input, boolean collapseOperands) {
        final List<EquationFrame> parsedInput = new ArrayList<>();

        if(input.contains(",")) {
            String[] parts = input.split(",");

            for(String p : parts) {
                if(p.length() > 1) {
                    parsedInput.add(new EquationFrame(false, p));
                } else if(p.length() > 0) {
                    char c = p.toCharArray()[0];

                    if(Operators.isOperator(c)) {
                        parsedInput.add(new EquationFrame(true, Operators.fromCharacter(c)));
                    } else {
                        parsedInput.add(new EquationFrame(false, String.valueOf(c)));
                    }
                }
            }

            return parsedInput;
        }

        StringBuilder operandBuffer = new StringBuilder();
        for(char c : input.toCharArray()) {
            if(c == ' ') {
                continue;
            }

            if(!Operators.isOperator(c)) {
                operandBuffer.append(c);

                if(!collapseOperands) {
                    parsedInput.add(new EquationFrame(false, operandBuffer.toString()));
                    operandBuffer.setLength(0);
                }
            } else {
                if(operandBuffer.length() > 0) {
                    parsedInput.add(new EquationFrame(false, operandBuffer.toString()));
                    operandBuffer.setLength(0);
                }

                parsedInput.add(new EquationFrame(true, Operators.fromCharacter(c)));
            }
        }

        if(operandBuffer.length() > 0) {
            parsedInput.add(new EquationFrame(false, operandBuffer.toString()));
        }

        return parsedInput;
    }

    public enum Operators {
        PLUS(         '+', 0,   Dir.BJ, 2, Double::sum     ),
        MINUS(        '-', 0,   Dir.BJ, 2, (a, b) -> a - b ),
        MULTIPLY(     '*', 1,   Dir.BJ, 2, (a, b) -> a * b ),
        DIVIDE(       '/', 1,   Dir.BJ, 2, (a, b) -> a / b ),
        MODULO(       '%', 1,   Dir.BJ, 2, (a, b) -> a % b ),
        EXPONENT(     '^', 2,   Dir.JB, 2, Math::pow       ),
        UNARY_MINUS(  '~', 3,   Dir.BJ, 1, (a, b) -> -b    ),
        OPEN_BRACKET( '(', 999, Dir.BJ, 0, (a, b) -> 0d    ),
        CLOSE_BRACKET(')', 999, Dir.BJ, 0, (a, b) -> 0d    ),
        EQUATION(     '=', -1 , Dir.BJ, 2, (a, b) -> b     );

        private final char operatorCharacter;
        private final int precedence;
        private final Dir direction;
        private final int numOperands;
        private final OperatorImpl impl;

        Operators(char operatorCharacter, int precedence, Dir direction, int numOperands, OperatorImpl impl) {
            this.operatorCharacter = operatorCharacter;
            this.precedence = precedence;
            this.direction = direction;
            this.numOperands = numOperands;
            this.impl = impl;
        }

        public char getOperatorCharacter() {
            return operatorCharacter;
        }

        public int getPrecedence() {
            return precedence;
        }

        public Dir getDirection() {
            return direction;
        }

        public int getNumOperands() {
            return numOperands;
        }

        public static Operators fromCharacter(char c) {
            Operators op = null;

            for(Operators op0 : values()) {
                if(c == op0.operatorCharacter) {
                    op = op0;
                    break;
                }
            }

            if(op == null) {
                throw new RuntimeException("Invalid character c!");
            }

            return op;
        }

        public static boolean isOperator(char c) {
            for(Operators op : values()) {
                if(c == op.operatorCharacter) {
                    return true;
                }
            }

            return false;
        }

        public enum Dir {
            BJ, JB
        }
    }

    public static class EquationFrame {
        private final boolean isOperator;
        private final Object data;

        private EquationFrame(boolean isOperator, Object data) {
            if(!(data instanceof Operators) && !(data instanceof String)) {
                throw new RuntimeException("Invalid frame!");
            }

            this.isOperator = isOperator;
            this.data = data;
        }

        public boolean isOperator() {
            return isOperator;
        }

        public Operators asOperator() {
            if(!(data instanceof Operators)) {
                throw new RuntimeException("!(data instanceof Operator)");
            }

            return (Operators) data;
        }

        public String asOperand() {
            if(!(data instanceof String)) {
                throw new RuntimeException("!(data instanceof String)");
            }

            return (String) data;
        }

        @Override
        public String toString() {
            if(isOperator()) {
                return String.valueOf(asOperator().operatorCharacter);
            } else {
                return asOperand();
            }
        }
    }

    public static class ConversionStep {
        private final List<EquationFrame> stackSnapshot;
        private final List<EquationFrame> outputSnapshot;
        private final EquationFrame nextFrame;

        private ConversionStep(List<EquationFrame> stackSnapshot, List<EquationFrame> outputSnapshot, EquationFrame nextFrame) {
            this.stackSnapshot = stackSnapshot;
            this.outputSnapshot = outputSnapshot;
            this.nextFrame = nextFrame;
        }

        public EquationFrame getNextFrame() {
            return nextFrame;
        }

        public List<EquationFrame> getStackSnapshot() {
            return stackSnapshot;
        }

        public List<EquationFrame> getOutputSnapshot() {
            return outputSnapshot;
        }
    }

    private interface OperatorImpl {
        double evaluate(double a, double b);
    }
}
