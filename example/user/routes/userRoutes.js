'use strict'

module.exports = app => {
    const userController = require('../controllers/userController')

    app.route('/users')
        .get(userController.listUsers)
        .post(userController.register)

    app.route('/employees')
        .post(userController.createEmployee)

    app.route('/users/:userId')
        .get(userController.getUser)
        .delete(userController.deleteUser)
}
