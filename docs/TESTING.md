# 🧪 AIsteroids Testing Guide

This document provides comprehensive information about the testing infrastructure and test suite for the AIsteroids game.

## 📋 **Table of Contents**
- [Overview](#overview)
- [Test Architecture](#test-architecture)
- [Running Tests](#running-tests)
- [Test Coverage](#test-coverage)
- [Adding New Tests](#adding-new-tests)
- [Testability Features](#testability-features)
- [Troubleshooting](#troubleshooting)

## 🎯 **Overview**

The AIsteroids project features a comprehensive test suite with **95+ automated tests** that verify game functionality, edge cases, and system robustness. The testing framework is built using pure Java without external dependencies.

### **Test Statistics**
- **Total Tests**: 95+ across all test suites
- **Test Suites**: 8 focused test files
- **Success Rate**: 100%
- **Coverage Areas**: All major game systems
- **Test Types**: Unit, Integration, Edge Case, Robustness, Component-focused

## 🏗️ **Test Architecture**

### **Test Structure**
```
AIsteroids/
├── src/                         # Main source code
├── test/                        # All test files
│   ├── IntegrationTestSuite.java    # Multi-system integration tests (16 tests)
│   ├── EdgeCaseTestSuite.java       # Edge cases & robustness (27 tests)
│   ├── ScoreCalculatorTest.java     # Score calculation tests (6 tests)
│   ├── CollisionTest.java           # Collision detection tests (9 tests)
│   ├── PowerUpTest.java             # Power-up system tests (9 tests)
│   ├── WaveSystemTest.java          # Wave system tests (12 tests)
│   ├── LeaderboardSystemTest.java   # Achievement/leaderboard tests (9 tests)
│   ├── BulletTest.java              # Bullet behavior tests (12 tests)
│   └── AsteroidTest.java            # Asteroid behavior tests (12 tests)
├── docs/TESTING.md              # This documentation
└── test.sh                      # Automated test runner script
```

### **Key Testing Components**

#### **1. TestRunner.java**
- **Purpose**: Core functionality validation
- **Tests**: 16 essential game mechanics
- **Focus**: Basic operations, happy path scenarios
- **Coverage**: ScoreCalculator, CollisionDetection, PowerUps, WaveSystem

#### **2. AdvancedTestRunner.java**
- **Purpose**: Edge cases and robustness testing
- **Tests**: 27 comprehensive scenarios
- **Focus**: Boundary conditions, error handling, system limits
- **Coverage**: All game objects, inheritance, complex interactions

#### **3. ScoreCalculator.java**
- **Purpose**: Extracted scoring logic for testability
- **Features**: Pure functions, deterministic results
- **Benefits**: Easy to test, no side effects, clear interfaces

## 🚀 **Running Tests**

### **Prerequisites**
```bash
# Ensure Java is installed and code is compiled
./build.sh
```

### **All Tests (Recommended)**
```bash
# Run complete test suite (95+ tests)
./test.sh
```

**Expected Output:**
```
🧪 Running AIsteroids Test Suite...

Testing ScoreCalculator...
  ✓ Small asteroid base score
  ✓ Medium asteroid base score
  ✓ Large asteroid base score
  ✓ Wave multiplier increases score
  ✓ Perfect wave gives bonus
  ✓ Speed bonus for fast completion
  ✓ Score multiplier calculation

Testing Collision Detection...
  ✓ Close objects collide
  ✓ Distant objects don't collide
  ✓ Object collides with itself

Testing PowerUp System...
  ✓ PowerUp types are defined
  ✓ PowerUp has valid position
  ✓ PowerUp is initially active

Testing Wave System...
  ✓ Wave system starts at wave 1
  ✓ Boss wave detection
  ✓ Asteroid count scales with wave

📊 Test Results:
Tests run: 16
Tests passed: 16
Tests failed: 0
🎉 ALL TESTS PASSED!
```

### **Advanced Test Suite**
```bash
# Run comprehensive edge case tests (27 tests)
java -ea AdvancedTestRunner
```

### **Full Test Suite**
```bash
# Run both test suites
java -ea TestRunner && echo "==========" && java -ea AdvancedTestRunner
```

### **Continuous Integration**
```bash
# Test script for CI/CD
#!/bin/bash
set -e
javac *.java
java -ea TestRunner
java -ea AdvancedTestRunner
echo "All tests passed! ✅"
```

## 📊 **Test Coverage**

### **System Coverage Matrix**

| System | Basic Tests | Advanced Tests | Total Coverage |
|--------|-------------|----------------|----------------|
| **GameObject** | ✅ | ✅✅✅ | Inheritance, positioning, wrapping |
| **CollisionDetection** | ✅✅✅ | ✅✅✅ | Standard + edge cases + zero radius |
| **ScoreCalculator** | ✅✅✅✅✅✅✅ | ✅✅✅✅ | All scoring scenarios + edge cases |
| **PowerUp System** | ✅✅✅ | ✅✅✅ | Types, mechanics, properties |
| **Wave System** | ✅✅✅ | ✅✅✅✅ | Progression, bosses, difficulty |
| **PlayerShip** | - | ✅✅✅✅ | Lives, damage, bullets, reset |
| **Bullet Behavior** | - | ✅✅✅ | Movement, expiration, collision |
| **Asteroid System** | - | ✅✅✅ | Sizes, movement, splitting |

### **Test Categories**

#### **Unit Tests** (Individual Components)
- ScoreCalculator methods
- GameObject behavior
- PowerUp properties
- Collision detection algorithms

#### **Integration Tests** (System Interactions)
- PlayerShip + Bullet interaction
- WaveSystem + ScoreCalculator integration
- PowerUp + PlayerShip mechanics

#### **Edge Case Tests** (Boundary Conditions)
- Zero radius collisions
- Maximum wave numbers
- Extreme coordinates
- Invalid inputs

#### **Robustness Tests** (Error Handling)
- System resets
- State transitions
- Resource limits
- Exception scenarios

## ➕ **Adding New Tests**

### **Adding to TestRunner**
```java
public void testNewFeature() {
    System.out.println("Testing New Feature...");

    testCase("New feature works correctly", () -> {
        // Arrange
        NewClass feature = new NewClass();

        // Act
        boolean result = feature.doSomething();

        // Assert
        return result == expectedValue;
    });
}
```

### **Adding to AdvancedTestRunner**
```java
public void testNewFeatureEdgeCases() {
    System.out.println("\nTesting New Feature Edge Cases...");

    testCase("Handles null input", () -> {
        NewClass feature = new NewClass();
        return feature.handleNull(null) == false;
    });

    testCase("Handles extreme values", () -> {
        NewClass feature = new NewClass();
        return feature.process(Integer.MAX_VALUE) > 0;
    });
}
```

### **Test Case Guidelines**
1. **Clear Names**: Describe what is being tested
2. **Arrange-Act-Assert**: Structure tests clearly
3. **Single Responsibility**: One assertion per test
4. **Edge Cases**: Include boundary conditions
5. **Error Handling**: Test failure scenarios

## 🔧 **Testability Features**

### **Dependency Injection**
```java
// GameObject supports dependency injection for testing
GameObject obj = new GameObject(x, y, mockCollisionDetector);
```

### **Test-Friendly Methods**
```java
// PlayerShip has test-friendly bullet firing
List<Bullet> bullets = ship.fireBulletForTesting(); // No rate limiting
```

### **Interfaces for Mocking**
```java
// CollisionDetector interface allows mocking
class MockCollisionDetector implements CollisionDetector {
    private boolean shouldCollide = false;

    public void setShouldCollide(boolean value) {
        this.shouldCollide = value;
    }

    @Override
    public boolean checkCollision(GameObject obj1, GameObject obj2) {
        return shouldCollide;
    }
}
```

### **Pure Functions**
```java
// ScoreCalculator uses pure functions
ScoreResult result = calculator.calculateAsteroidScore(radius, wave, multiplier);
// Same inputs always produce same outputs
```

## 🐛 **Troubleshooting**

### **Common Issues**

#### **Assertion Failures**
```bash
❌ Test failed - Assertion failed
```
- Check test logic and expected values
- Verify game object states
- Review recent code changes

#### **Compilation Errors**
```bash
javac *.java
# Check for syntax errors or missing imports
```

#### **Test Exceptions**
```bash
❌ Test failed - Exception: NullPointerException
```
- Check for null references
- Verify object initialization
- Review method parameters

### **Debug Tips**

#### **Add Debug Output**
```java
testCase("Debug test", () -> {
    System.out.println("Debug: value = " + value);
    return value > 0;
});
```

#### **Verify Test Environment**
```bash
# Check Java version and assertions
java -version
java -ea -cp . TestRunner  # Ensure assertions are enabled
```

#### **Isolate Failing Tests**
```java
// Comment out other tests to isolate issues
public void runAllTests() {
    // testScoreCalculator();
    // testCollisionDetection();
    testProblemArea();  // Focus on failing area
}
```

## 📈 **Metrics and Reporting**

### **Test Metrics**
- **Execution Time**: ~1-2 seconds for full suite
- **Memory Usage**: Minimal (pure Java objects)
- **Success Rate**: Target 100%
- **Coverage**: All major game systems

### **Performance Benchmarks**
```bash
# Measure test execution time
time java -ea TestRunner
time java -ea AdvancedTestRunner
```

### **Quality Gates**
- All tests must pass before commits
- New features require corresponding tests
- Edge cases must be covered for critical systems
- Documentation must be updated for new test patterns

## 🎯 **Best Practices**

### **Test Design**
1. **Fast**: Tests should run quickly
2. **Independent**: Tests should not depend on each other
3. **Repeatable**: Same results every time
4. **Self-Validating**: Clear pass/fail results
5. **Timely**: Written alongside code

### **Maintenance**
1. **Keep tests simple** and focused
2. **Update tests** when code changes
3. **Remove obsolete tests** promptly
4. **Document complex test scenarios**
5. **Review test coverage** regularly

### **Code Quality**
1. **Use meaningful test names**
2. **Keep test methods small**
3. **Avoid test code duplication**
4. **Use helper methods** for common setup
5. **Comment complex test logic**

---

**Happy Testing! 🧪✨**

*For questions or issues with testing, please check the troubleshooting section or review the test implementation in `TestRunner.java` and `AdvancedTestRunner.java`.*
