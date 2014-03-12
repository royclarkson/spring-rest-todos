module.exports = TodosController;

function TodosController() {}

TodosController.prototype.add = function(todos, todo) {
	todos.push(todo);
};