var registry = require('rest/mime/registry');
var json = require('rest/mime/type/application/json');
var fluent = require('wire/config/fluent');
var JsonPatch = require('cola/data/JsonPatch');
var TodosController = require('./TodosController');

module.exports = fluent(function(context) {
	return context
		.add('@init', function() {
			return registry.register('application/json-patch+json', json)
		})
		.add('todos@controller', TodosController)
		.add('todos@model', function() {
			return new JsonPatch('/todos');
		});
});