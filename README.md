# Event Manager API

A robust REST API for managing events with weather information, built with Spring Boot.

## Features

- **Event Management**: Create, read, update, and delete events
- **Pagination**: Efficiently handle large datasets with paginated responses
- **Search Capabilities**: Find events by:
  - Title (case-insensitive)
  - Location (case-insensitive)
  - Date range
- **Weather Integration**: Automatic weather data fetching for event locations
- **Caching**: Optimized performance with Caffeine cache
- **Database Optimization**: Indexed fields for faster queries
- **RESTful Design**: Follows REST best practices and conventions

## Prerequisites

- Java 21 
- Maven 3.6 or higher
- H2 Database (included for development)

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/eventmanager.git
cd eventmanager
```

### 2. Build the Project
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Events

#### Get All Events
```http
GET /api/events?page=0&size=10&sortBy=startDate&direction=desc
```

#### Search Events
```http
GET /api/events/search?title=Conference&location=New York&startDate=2024-04-01T00:00:00&endDate=2024-04-30T23:59:59&page=0&size=10
```

#### Get Event by ID
```http
GET /api/events/{id}
```

#### Create Event
```http
POST /api/events
Content-Type: application/json

{
    "title": "Spring Conference 2024",
    "description": "Annual Spring Framework Conference",
    "location": "New York",
    "startDate": "2024-04-15T09:00:00",
    "endDate": "2024-04-17T17:00:00"
}
```

#### Update Event
```http
PUT /api/events/{id}
Content-Type: application/json

{
    "title": "Updated Conference Title",
    "description": "Updated description",
    "location": "Updated Location",
    "startDate": "2024-04-15T09:00:00",
    "endDate": "2024-04-17T17:00:00"
}
```

#### Delete Event
```http
DELETE /api/events/{id}
```

## Response Format

### Paginated Response
```json
{
    "content": [
        {
            "id": 1,
            "title": "Event Title",
            "description": "Event Description",
            "location": "Event Location",
            "startDate": "2024-04-15T09:00:00",
            "endDate": "2024-04-17T17:00:00",
            "weatherData": "Weather information",
            "createdAt": "2024-03-20T10:00:00",
            "updatedAt": "2024-03-20T10:00:00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
            "sorted": true,
            "unsorted": false,
            "empty": false
        },
        "offset": 0,
        "unpaged": false,
        "paged": true
    },
    "totalElements": 15,
    "totalPages": 2,
    "last": false,
    "first": true,
    "empty": false,
    "numberOfElements": 10,
    "size": 10,
    "number": 0,
    "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
    }
}
```

## Configuration

### Application Properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:eventdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
spring.jpa.hibernate.ddl-auto=update

# Cache Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=600s

# Logging Configuration
logging.level.org.springframework=INFO
logging.level.com.technology309=DEBUG
```

## Testing

### Run Tests
```bash
mvn test
```

The test suite includes:
- Integration tests for all API endpoints
- Unit tests for service layer
- Cache behavior verification
- Pagination and search functionality tests

## Performance Considerations

- **Caching**: Implements Caffeine cache for frequently accessed data
- **Database Indexes**: Optimized queries with indexes on:
  - `start_date`
  - `location`
  - `title`
- **Pagination**: Efficient handling of large datasets
- **Case-insensitive Search**: Optimized search queries

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- H2 Database for the in-memory database
- Caffeine for the caching solution 