# Smart Parking Management System

A comprehensive web-based parking management system built with Spring Boot and Thymeleaf, featuring role-based authentication, real-time slot management, and comprehensive admin dashboards.

## Features

### 🔐 Authentication & Authorization
- Role-based access control (Admin & Driver)
- Secure password encryption with BCrypt
- Custom authentication success handlers
- Session management and logout functionality

### 👨‍💼 Admin Features
- **Dashboard**: KPIs, charts, and system overview
- **Driver Management**: Add, edit, delete, and manage driver accounts
- **Parking Slots**: Comprehensive slot management with status tracking
- **Reservations**: View and manage all booking reservations
- **Payments & Revenue**: Financial tracking with charts and analytics
- **Reports & Analytics**: Generate custom reports and view system analytics

### 🚗 Driver Features
- **Personal Dashboard**: Real-time statistics with auto-refresh functionality
- **Slot Booking**: Reserve parking slots with real-time availability
- **Booking Management**: View booking history with 5-minute cancellation window
- **Payment Integration**: Secure Stripe payment processing with minimum amount handling
- **Profile Management**: Update personal information and change password

### 🎨 UI/UX Features
- Modern, responsive Bootstrap 5 design
- Interactive charts using Chart.js
- Custom CSS with gradient themes
- Mobile-friendly responsive layout
- Accessibility features and high contrast support
- Dark mode support
- Print-friendly styles

## Technology Stack

- **Backend**: Spring Boot 3.5.5, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5, Font Awesome, Chart.js
- **Database**: MySQL (development), PostgreSQL (production)
- **Payment**: Stripe API integration
- **Build Tool**: Maven
- **Java Version**: 17

## Project Structure

```
src/
├── main/
│   ├── java/com/example/park/
│   │   ├── config/          # Security and application configuration
│   │   ├── controller/      # Web controllers (Auth, Admin, Driver)
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities (User, ParkingSlot, Booking, Payment)
│   │   ├── repository/     # Data repositories
│   │   ├── service/        # Business logic services
│   │   └── ParkApplication.java
│   └── resources/
│       ├── static/css/     # Custom CSS styles
│       ├── templates/      # Thymeleaf templates
│       │   ├── admin/      # Admin dashboard templates
│       │   ├── driver/     # Driver dashboard templates
│       │   ├── index.html  # Landing page
│       │   ├── login.html  # Login page
│       │   └── register.html # Registration page
│       └── application.properties
```

## Database Configuration

The application is configured to use MySQL with the following settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/transport_db
spring.datasource.username=root
spring.datasource.password=123456
```

## Default Admin Account

- **Username**: admin
- **Password**: Admin@123

## Getting Started

### Prerequisites
- Java 24
- MySQL 8.0+
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd park
   ```

2. **Setup Database**
   - Create MySQL database named `transport_db`
   - Update database credentials in `application.properties` if needed

3. **Build and Run**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the Application**
   - Open browser and navigate to `http://localhost:8080`
   - Use admin credentials to access admin panel
   - Register as a driver to access driver features

## Key Components

### Security Configuration
- BCrypt password encoding
- Role-based URL protection
- Custom authentication success handler
- CSRF protection (disabled for development)

### Controllers
- **AuthController**: Landing page, login, registration
- **AdminController**: Admin dashboard and driver management
- **DriverController**: Driver dashboard and profile management

### Entities
- **User**: Authentication and user management
- **ParkingSlot**: Parking space management (ready for implementation)
- **Booking**: Reservation system (ready for implementation)
- **Payment**: Payment processing (ready for implementation)

### Templates

#### Admin Templates
- `dashboard.html`: Main admin dashboard with KPIs and charts
- `drivers.html`: Driver management list
- `add-driver.html`: Add new driver form
- `edit-driver.html`: Edit driver information
- `parking-slots.html`: Parking slot management
- `bookings.html`: Reservation management
- `payments.html`: Payment and revenue management
- `reports.html`: Analytics and reporting

#### Driver Templates
- `dashboard.html`: Driver main dashboard
- `profile.html`: Driver profile view
- `edit-profile.html`: Profile editing form
- `change-password.html`: Password change form

### Styling
- Custom CSS with CSS variables for theming
- Responsive design for all screen sizes
- Bootstrap 5 integration
- Chart.js for data visualization
- Font Awesome icons

## Features Ready for Backend Integration

The following UI components are ready and waiting for backend integration:

1. **Parking Slot Management**: Complete CRUD operations UI
2. **Booking System**: Reservation management interface
3. **Payment Processing**: Payment tracking and revenue analytics
4. **Real-time Updates**: WebSocket integration points identified
5. **Report Generation**: Custom report builder interface

## Development Notes

### Completed Features
- ✅ User authentication and authorization
- ✅ Admin dashboard with comprehensive management
- ✅ Driver profile and dashboard
- ✅ Responsive UI design
- ✅ Security configuration
- ✅ Database integration setup
- ✅ Template structure and styling

### Next Steps for Full Implementation
1. Implement ParkingSlot entity operations
2. Add Booking system backend logic
3. Integrate Payment processing
4. Add real-time slot availability updates
5. Implement report generation backend
6. Add email notifications
7. Deploy to production environment

## API Endpoints

### Authentication
- `GET /` - Landing page
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `POST /logout` - Logout

### Admin Routes (Role: ADMIN)
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/drivers` - Driver management
- `POST /admin/drivers/add` - Add new driver
- `GET /admin/drivers/edit/{id}` - Edit driver form
- `POST /admin/drivers/edit/{id}` - Update driver
- `DELETE /admin/drivers/delete/{id}` - Delete driver

### Driver Routes (Role: DRIVER)
- `GET /driver/dashboard` - Driver dashboard
- `GET /driver/profile` - View profile
- `GET /driver/profile/edit` - Edit profile form
- `POST /driver/profile/edit` - Update profile
- `GET /driver/change-password` - Change password form
- `POST /driver/change-password` - Update password

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Support

For support and questions, please contact the development team or create an issue in the repository.
