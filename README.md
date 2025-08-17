# Salary Slip Generator

A Spring Boot application for automated salary slip generation from Excel data with both scheduled and on-demand processing capabilities.

## Features

- Excel file processing with batch support for large datasets
- PDF salary slip generation with customizable templates
- Scheduled automatic processing (configurable)
- REST API for manual processing
- Swagger/OpenAPI documentation
- Caching for improved performance
- Error handling and notifications
- Configurable startup behavior

## Prerequisites

- Java 21 or higher
- Maven 3.6.3 or higher
- Git (optional)

## Project Structure

```
salary-slip/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cavin/salary_slip/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── constants/       # Application constants
│   │   │       ├── controller/      # REST endpoints
│   │   │       ├── model/          # Data models
│   │   │       ├── scheduler/      # Scheduling components
│   │   │       └── service/        # Business logic
│   │   └── resources/
│   │       ├── application.properties  # Application configuration
│   │       └── static/
│   │           └── img.png            # Company logo
│   └── test/                          # Test classes
└── pom.xml                            # Maven configuration
```

## Configuration

### Application Properties

```properties
# Logo Configuration
salary.slip.logo.path=static/img.png

# File Paths
salary.slip.excel.path=C:/workspace/JulySalaryEPF1.xlsx
salary.slip.output.dir=C:/workspace/slips/
salary.slip.excel.sheet.name=Sheet1

# Scheduler Configuration
salary.slip.scheduler.enabled=true           # Enable/disable scheduler
salary.slip.scheduler.cron=0 0 10 * * ?     # Run at 10 AM daily
salary.slip.generate.on.startup=false        # Generate on app startup
salary.slip.notification.email=admin@example.com

# Thread Pool Configuration
salary.slip.scheduler.pool-size=5
salary.slip.scheduler.thread-name-prefix=SalarySlipScheduler-
salary.slip.scheduler.await-termination=60
```

## Setup and Running

### Using IntelliJ IDEA

1. Open Project:
   - File → Open → Navigate to salary-slip folder
   - Select pom.xml → Open as Project

2. Configure JDK:
   - File → Project Structure → Project
   - Set SDK to Java 21
   - Set Language Level to 21

3. Install Dependencies:
   - Right-click on pom.xml
   - Maven → Reload Project

4. Configure Run Configuration:
   - Run → Edit Configurations
   - Add New Configuration (+) → Spring Boot
   - Main class: `com.cavin.salary_slip.SalarySlipApplication`
   - Name: `SalarySlipApplication`

5. Run the Application:
   - Click the green play button or press Shift+F10

### Using Terminal

1. Build the Project:
```bash
# Navigate to project directory
cd salary-slip

# Build with Maven
mvn clean install
```

2. Run the Application:
```bash
# Using Maven
mvn spring-boot:run

# Using Java
java -jar target/salary-slip-1.0.0.jar
```

## API Documentation

Access the Swagger UI at: `http://localhost:8080/swagger-ui.html`

### Available Endpoints

1. Generate Salary Slips:
```http
POST /api/salary-slip/generate
```
- Parameters:
  - `file`: Excel file (required)
  - `sheetName`: Sheet name (optional)

2. Get Sheet Names:
```http
GET /api/salary-slip/sheets
```
- Parameters:
  - `file`: Excel file (required)

## Excel File Format

The input Excel file should have the following columns:

1. Employee ID
2. Name
3. Designation
4. Bank Account
5. IFSC Code
6. UAN Number
7. Payable Days
8. Salary Date
9. PAN Number
10. Aadhar Number
11. Basic Salary
12. HRA
13. DA
14. Special Allowance
15. Travel Allowance
16. Income Tax
17. EPF
18. Leave Deduction

## Scheduler Configuration

The scheduler can be configured in multiple ways:
### application.properties
```properties
salary.slip.scheduler.enabled=true
salary.slip.scheduler.cron=0 0 10 * * ?
```

Common cron expressions:
- `0 0 10 * * ?` - Every day at 10 AM
- `0 0 0 1 * ?` - First day of every month at midnight
- `0 0 */2 * * ?` - Every 2 hours

## Output Directory Structure

```
slips/
└── batch_YYYYMMDD_HHMMSS/
    ├── EmpName1_SalarySlip.pdf
    ├── EmpName2_SalarySlip.pdf
    └── ...
```

## Error Handling

The application includes:
- Automatic retry for failed operations
- Email notifications for errors
- Detailed logging
- Batch status tracking
- Validation of input data

## Performance Optimization

- Batch processing for large files
- Configurable thread pool
- Caching of frequently used data
- Memory-efficient Excel reading
- Automatic cleanup of old batches

## Logging

Logs are available in:
- Console output
- Application logs (configured via application.properties)

## Development

### Adding New Features

1. Create new components in appropriate packages
2. Update AppConstants.java for new constants
3. Add configuration in application.properties
4. Update tests accordingly

### Building for Production

```bash
# Package with production profile
mvn clean package -Pprod

# Run with production profile
java -jar target/salary-slip-1.0.0.jar --spring.profiles.active=prod
```

## Troubleshooting

Common issues and solutions:

1. Excel File Not Found:
   - Verify file path in application.properties
   - Check file permissions

2. PDF Generation Fails:
   - Verify output directory permissions
   - Check available disk space

3. Scheduler Not Running:
   - Verify scheduler.enabled property
   - Check cron expression
   - Verify system time

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
