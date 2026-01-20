# Bill Splitting MCP Server

A Model Context Protocol (MCP) server built with Spring AI 1.1.2 for splitting bills among friends. This server provides comprehensive expense tracking and calculation capabilities with automatic settlement recommendations.

## Features

- **Expense Group Management**: Create and manage expense groups for different trips or events
- **Member Management**: Add and remove members from expense groups
- **Expense Tracking**: Record expenses with descriptions, amounts (INR), and payer information
- **Flexible Splitting**: Support for equal, amount-based, and percentage-based expense splitting
- **Financial Calculations**: Automatic calculation of member balances and settlement recommendations
- **MCP Integration**: Seamless integration with Claude Desktop via Model Context Protocol

## Technology Stack

- **Framework**: Spring Boot 3.2.1 with Spring AI 1.1.2
- **Database**: PostgreSQL (Docker container)
- **Protocol**: Model Context Protocol (MCP)
- **Currency**: Indian Rupees (INR)
- **Testing**: JUnit 5, Mockito, jqwik (Property-based testing)

## Prerequisites

- Java 21 or higher
- Maven 3.6 or higher
- Docker and Docker Compose
- Claude Desktop (for MCP integration)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd bill-splitting-mcp-server
```

### 2. Start PostgreSQL Database

```bash
docker-compose up -d
```

This will start a PostgreSQL container with the following configuration:
- Database: `billsplitting`
- Username: `billsplitter`
- Password: `password`
- Port: `5432`

### 3. Build and Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on port 8080 and automatically run database migrations.

### 4. Configure Claude Desktop

Add the following configuration to your Claude Desktop MCP settings:

```json
{
  "mcpServers": {
    "bill-splitting": {
      "command": "java",
      "args": ["-jar", "target/bill-splitting-mcp-server-1.0.0.jar"],
      "env": {
        "SPRING_PROFILES_ACTIVE": "local"
      }
    }
  }
}
```

## Available MCP Tools

### Group Management
- `create_expense_group` - Create a new expense group
- `list_expense_groups` - List all expense groups

### Member Management
- `add_group_member` - Add a member to an expense group
- `remove_group_member` - Remove a member from an expense group
- `list_group_members` - List all members in a group

### Expense Management
- `add_expense` - Add an expense to a group
- `update_expense` - Update an existing expense
- `delete_expense` - Delete an expense
- `list_expenses` - List all expenses for a group

### Splitting Tools
- `split_expense_equally` - Split an expense equally among all members
- `split_expense_by_amount` - Split an expense by custom amounts
- `split_expense_by_percentage` - Split an expense by percentage shares

### Calculation and Reporting
- `calculate_group_totals` - Calculate total expenses and member balances
- `get_member_balance` - Get balance details for a specific member
- `generate_settlement_summary` - Generate settlement recommendations
- `get_expense_history` - Get expense history for a group

## Usage Examples

### Creating a Group and Adding Members

```
Create an expense group called "Goa Trip 2024" with description "Friends trip to Goa"
Add members: Alice, Bob, Charlie, Diana
```

### Adding and Splitting Expenses

```
Add expense: "Hotel booking" for ₹8000 paid by Alice
Split the hotel expense equally among all members

Add expense: "Dinner at restaurant" for ₹1200 paid by Bob
Split dinner expense: Alice 30%, Bob 25%, Charlie 25%, Diana 20%
```

### Getting Settlement Summary

```
Generate settlement summary for "Goa Trip 2024"
```

This will provide optimal settlement recommendations to minimize the number of transactions needed.

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Property-Based Tests
```bash
mvn test -Dtest=*PropertyTest
```

The property-based tests validate mathematical correctness of splitting algorithms:
- **Split Conservation**: Sum of splits equals original expense amount
- **Equal Split Fairness**: Maximum difference between member amounts ≤ 0.01 INR
- **Percentage Split Accuracy**: Each member's amount within 0.01 INR of percentage share

## Database Schema

The application uses four main tables:
- `expense_groups` - Store expense group information
- `group_members` - Store group membership data
- `expenses` - Store individual expense records
- `expense_splits` - Store how expenses are split among members

## Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/billsplitting
    username: billsplitter
    password: password
  ai:
    mcp:
      server:
        enabled: true
        name: bill-splitting-server
        version: 1.0.0
```

### Environment Variables

- `SPRING_PROFILES_ACTIVE` - Set to `local` for development
- `DATABASE_URL` - Override default database URL
- `DATABASE_USERNAME` - Override default database username
- `DATABASE_PASSWORD` - Override default database password

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure PostgreSQL container is running: `docker-compose ps`
   - Check database credentials in `application.yml`

2. **MCP Tools Not Detected**
   - Verify Spring AI MCP dependency is included
   - Check that tool classes are annotated with `@McpTool`
   - Ensure classes are in the component scan path

3. **Claude Desktop Integration Issues**
   - Verify MCP server configuration in Claude Desktop
   - Check application logs for startup errors
   - Ensure the JAR file path is correct in the configuration

### Logs

Application logs are available at:
- Console output during development
- Application logs show MCP tool registration and execution

## Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.