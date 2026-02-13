# Generated with Water Generator
# The Goal of feature test is to ensure the correct format of json responses
# If you want to perform functional test please refer to ApiTest
Feature: Check Role Rest Api Response

  Scenario: Role CRUD Operations

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/roles'
    # ---- Add entity fields here -----
    And request { "name": "exampleField","description":"role"}
    # ---------------------------------
    When method POST
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "name":"exampleField",
        "entityVersion":1,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "description": "role",
        "categoryIds": #null,
        "tagIds": #null
       }
    """
    * def entityId = response.id
    
    # --------------- UPDATE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/roles'
    # ---- Add entity fields here -----
    And request { "id":"#(entityId)","entityVersion":1,"name": "nameUpdated","description":"description"}
    # ---------------------------------
    When method PUT
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "name": "nameUpdated",
        "description":"description",
        "categoryIds": #null,
        "tagIds": #null
       }
    """

  # --------------- FIND -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/roles/'+entityId
    # ---------------------------------
    When method GET
    Then status 200
    # ---- Matching required response json ----
    And match response ==
    """
      { "id": #number,
        "entityVersion":2,
        "entityCreateDate":'#number',
        "entityModifyDate":'#number',
        "name": 'nameUpdated',
        "description":"description",
        "categoryIds": #null,
        "tagIds": #null
       }
    """

  # --------------- FIND ALL -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/roles'
    When method GET
    Then status 200
    And match response.results contains
    """
    {
      "id": #number,
      "entityVersion":2,
      "entityCreateDate":'#number',
      "entityModifyDate":'#number',
      "name": 'nameUpdated',
      "description":"description",
      "categoryIds": #null,
      "tagIds": #null
    }
    """

  # --------------- DELETE -----------------------------

    Given header Content-Type = 'application/json'
    And header Accept = 'application/json'
    Given url serviceBaseUrl+'/water/roles/'+entityId
    When method DELETE
    # 204 because delete response is empty, so the status code is "no content" but is ok
    Then status 204
