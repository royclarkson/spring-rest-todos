module.exports = TodosController;

function TodosController() {}

TodosController.prototype.add = function(todos, todo) {
	todos.push(todo);
};

TodosController.prototype.remove = function(todos, todo) {
	todos.some(function(t, i, todos) {
		if(todo.id === t.id) {
			todos.splice(i, 1);
			return true;
		}
	});
};

TodosController.prototype.removeCompleted = function(todos) {
	return todos.filter(function(todo) {
		return !todo.complete;
	});
};

TodosController.prototype.completeAll = function(todos) {
	todos.forEach(function(todo) {
		todo.complete = true;
	});
};

