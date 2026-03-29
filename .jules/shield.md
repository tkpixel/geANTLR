# Shield's Journal

2026-03-29 - [JaCoCo Java 25 Compatibility]
Learning: JaCoCo versions 0.8.12 and below do not support Java 25 class file versions natively, throwing an Unsupported class file major version 69 error.
Action: Temporarily skipping jacoco execution using -Djacoco.skip=true for Java 25 compatibility unless using an experimental build of JaCoCo or adjusting the project SDK compatibility if tests must pass coverage thresholds.
